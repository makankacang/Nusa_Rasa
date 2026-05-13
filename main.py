from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import timedelta
from dotenv import load_dotenv

from database import engine, get_db
import models, schemas, crud
from auth import create_access_token, get_current_admin, ACCESS_TOKEN_EXPIRE_MINUTES

load_dotenv()

models.Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="NusaRasa API",
    version="1.0.0",
    description="REST API dengan JWT Authentication untuk aplikasi pemesanan makanan NusaRasa"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ═══════════════════════════════════════════
# AUTH — ADMIN
# ═══════════════════════════════════════════

@app.post("/api/v1/auth/register", response_model=schemas.AdminOut, status_code=201, tags=["Auth"])
def register(data: schemas.AdminRegister, db: Session = Depends(get_db)):
    if crud.get_admin_by_email(db, data.email):
        raise HTTPException(status_code=400, detail="Email sudah terdaftar")
    return crud.create_admin(db, data)

@app.post("/api/v1/auth/login", response_model=schemas.TokenResponse, tags=["Auth"])
def login(data: schemas.AdminLogin, db: Session = Depends(get_db)):
    admin = crud.authenticate_admin(db, data.email, data.password)
    if not admin:
        raise HTTPException(status_code=401, detail="Email atau password salah")
    token = create_access_token(
        data={"sub": str(admin.id)},
        expires_delta=timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    )
    return {"access_token": token, "token_type": "bearer", "admin_id": admin.id, "name": admin.name}

@app.get("/api/v1/auth/me", response_model=schemas.AdminOut, tags=["Auth"])
def get_me(admin: models.Admin = Depends(get_current_admin)):
    return admin


# ═══════════════════════════════════════════
# CATEGORIES
# ═══════════════════════════════════════════

@app.get("/api/v1/categories", response_model=List[schemas.CategoryOut], tags=["Categories"])
def get_categories(db: Session = Depends(get_db)):
    return crud.get_all_categories(db)

@app.post("/api/v1/categories", response_model=schemas.CategoryOut, status_code=201, tags=["Categories"])
def create_category(
    data: schemas.CategoryCreate,
    db: Session = Depends(get_db),
    _: models.Admin = Depends(get_current_admin)
):
    return crud.create_category(db, data)

@app.delete("/api/v1/categories/{category_id}", response_model=schemas.MessageResponse, tags=["Categories"])
def delete_category(
    category_id: int,
    db: Session = Depends(get_db),
    _: models.Admin = Depends(get_current_admin)
):
    crud.delete_category(db, category_id)
    return {"message": "Kategori berhasil dihapus"}


# ═══════════════════════════════════════════
# MENUS
# ═══════════════════════════════════════════

@app.get("/api/v1/menus", response_model=List[schemas.MenuOut], tags=["Menus"])
def get_menus(category: Optional[str] = None, db: Session = Depends(get_db)):
    return crud.get_all_menus(db, category)

@app.get("/api/v1/menus/{menu_id}", response_model=schemas.MenuOut, tags=["Menus"])
def get_menu(menu_id: int, db: Session = Depends(get_db)):
    menu = crud.get_menu(db, menu_id)
    if not menu:
        raise HTTPException(status_code=404, detail="Menu tidak ditemukan")
    return menu

@app.post("/api/v1/menus", response_model=schemas.MenuOut, status_code=201, tags=["Menus"])
def create_menu(
    data: schemas.MenuCreate,
    db: Session = Depends(get_db),
    admin: models.Admin = Depends(get_current_admin)
):
    return crud.create_menu(db, data)

@app.put("/api/v1/menus/{menu_id}", response_model=schemas.MenuOut, tags=["Menus"])
def update_menu(
    menu_id: int,
    data: schemas.MenuCreate,
    db: Session = Depends(get_db),
    admin: models.Admin = Depends(get_current_admin)
):
    updated = crud.update_menu(db, menu_id, data)
    if not updated:
        raise HTTPException(status_code=404, detail="Menu tidak ditemukan")
    return updated

@app.delete("/api/v1/menus/{menu_id}", response_model=schemas.MessageResponse, tags=["Menus"])
def delete_menu(
    menu_id: int,
    db: Session = Depends(get_db),
    admin: models.Admin = Depends(get_current_admin)
):
    crud.delete_menu(db, menu_id)
    return {"message": "Menu berhasil dihapus"}


# ═══════════════════════════════════════════
# ORDERS — publik (tidak perlu login)
# ═══════════════════════════════════════════

@app.post("/api/v1/orders", response_model=schemas.OrderOut, status_code=201, tags=["Orders"])
def create_order(data: schemas.OrderCreate, db: Session = Depends(get_db)):
    return crud.create_order(db, data)

@app.get("/api/v1/orders/{order_id}", response_model=schemas.OrderOut, tags=["Orders"])
def get_order(order_id: int, db: Session = Depends(get_db)):
    order = crud.get_order(db, order_id)
    if not order:
        raise HTTPException(status_code=404, detail="Pesanan tidak ditemukan")
    return order


# ═══════════════════════════════════════════
# ORDERS — admin only
# ═══════════════════════════════════════════

@app.get("/api/v1/admin/orders", response_model=List[schemas.OrderOut], tags=["Admin - Orders"])
def get_all_orders(
    status: Optional[str] = None,
    db: Session = Depends(get_db),
    admin: models.Admin = Depends(get_current_admin)
):
    return crud.get_all_orders(db, status)

@app.put("/api/v1/admin/orders/{order_id}/status", response_model=schemas.OrderOut, tags=["Admin - Orders"])
def update_order_status(
    order_id: int,
    data: schemas.OrderStatusUpdate,
    db: Session = Depends(get_db),
    admin: models.Admin = Depends(get_current_admin)
):
    order = crud.update_order_status(db, order_id, data.status)
    if not order:
        raise HTTPException(status_code=404, detail="Pesanan tidak ditemukan")
    return order


# ═══════════════════════════════════════════
# PAYMENTS — admin only
# ═══════════════════════════════════════════

@app.post("/api/v1/admin/payments/{order_id}", response_model=schemas.PaymentOut, status_code=201, tags=["Admin - Payments"])
def create_payment(
    order_id: int,
    data: schemas.PaymentCreate,
    db: Session = Depends(get_db),
    admin: models.Admin = Depends(get_current_admin)
):
    payment = crud.create_payment(db, order_id, data)
    if not payment:
        raise HTTPException(status_code=404, detail="Pesanan tidak ditemukan")
    return payment

@app.get("/api/v1/payments/order/{order_id}", response_model=schemas.PaymentOut, tags=["Payments"])
def get_payment(order_id: int, db: Session = Depends(get_db)):
    payment = crud.get_payment_by_order(db, order_id)
    if not payment:
        raise HTTPException(status_code=404, detail="Data pembayaran tidak ditemukan")
    return payment

@app.put("/api/v1/admin/payments/{payment_id}/confirm", response_model=schemas.PaymentOut, tags=["Admin - Payments"])
def confirm_payment(
    payment_id: int,
    db: Session = Depends(get_db),
    admin: models.Admin = Depends(get_current_admin)
):
    payment = crud.confirm_payment(db, payment_id)
    if not payment:
        raise HTTPException(status_code=404, detail="Data pembayaran tidak ditemukan")
    return payment


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
