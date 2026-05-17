"""
NusaRasa API — updated endpoint paths sesuai ApiService.kt Android

Perbandingan path lama vs baru:
  POST /api/v1/auth/login          → POST /auth/login
  GET  /api/v1/admin/orders        → GET  /orders/
  GET  /api/v1/orders/{id}         → GET  /orders/{id}
  PUT  /api/v1/admin/orders/{id}/status → PATCH /orders/{id}/status
  GET  /api/v1/menus               → GET  /menu/
  POST /api/v1/menus               → POST /menu/   (multipart)
  PUT  /api/v1/menus/{id}          → PUT  /menu/{id} (multipart)
  (baru) GET /dashboard/stats
  (baru) GET /payments/
  (baru) PATCH /menu/{id}/availability

Jalankan:
    pip install fastapi uvicorn sqlalchemy pymysql python-jose passlib[bcrypt] python-dotenv python-multipart
    uvicorn main:app --host 0.0.0.0 --port 8000 --reload
"""

from fastapi import FastAPI, Depends, HTTPException, Form, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import timedelta
from dotenv import load_dotenv
import os

from database import engine, get_db
import models, schemas, crud
from auth import create_access_token, get_current_admin, ACCESS_TOKEN_EXPIRE_MINUTES

load_dotenv()
models.Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="NusaRasa API",
    version="2.0.0",
    description="API untuk aplikasi pemesanan NusaRasa — endpoint disesuaikan dengan Android app"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ═══════════════════════════════════════════════════════════
#  AUTH
#  POST /auth/login  →  { token, admin: {id, name, email} }
# ═══════════════════════════════════════════════════════════

@app.post("/auth/login", response_model=schemas.TokenResponse, tags=["Auth"])
def login(data: schemas.AdminLogin, db: Session = Depends(get_db)):
    admin = crud.authenticate_admin(db, data.email, data.password)
    if not admin:
        raise HTTPException(status_code=401, detail="Email atau password salah")
    token = create_access_token(
        data={"sub": str(admin.id)},
        expires_delta=timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    )
    return {
        "token": token,
        "admin": {"id": admin.id, "name": admin.name, "email": admin.email}
    }

@app.post("/auth/register", response_model=schemas.AdminOut, status_code=201, tags=["Auth"])
def register(data: schemas.AdminRegister, db: Session = Depends(get_db)):
    if crud.get_admin_by_email(db, data.email):
        raise HTTPException(status_code=400, detail="Email sudah terdaftar")
    admin = crud.create_admin(db, data)
    return schemas.AdminOut(id=admin.id, name=admin.name, email=admin.email)

@app.get("/auth/me", tags=["Auth"])
def get_me(admin: models.Admin = Depends(get_current_admin)):
    return {"id": admin.id, "name": admin.name, "email": admin.email}


# ═══════════════════════════════════════════════════════════
#  DASHBOARD
#  GET /dashboard/stats
# ═══════════════════════════════════════════════════════════

@app.get("/dashboard/stats", response_model=schemas.DashboardStats, tags=["Dashboard"])
def get_dashboard_stats(
    admin: models.Admin = Depends(get_current_admin),
    db: Session = Depends(get_db)
):
    all_orders = crud.get_all_orders(db)

    def count(status): return sum(
        1 for o in all_orders
        if (o.status.value if hasattr(o.status, 'value') else str(o.status)).lower() == status
    )

    paid_orders = [
        o for o in all_orders
        if (o.status.value if hasattr(o.status, 'value') else str(o.status)).lower() in ("paid", "done")
    ]
    revenue = int(sum(o.total_price for o in paid_orders))
    recent = sorted(all_orders, key=lambda x: x.created_at, reverse=True)[:3]

    return schemas.DashboardStats(
        total_orders=len(all_orders),
        pending=count("pending"),
        approved=count("approved"),
        paid=count("paid"),
        done=count("done"),
        revenue=revenue,
        revenue_transactions=len(paid_orders),
        recent_orders=[schemas.OrderOut.from_db(o) for o in recent],
    )


# ═══════════════════════════════════════════════════════════
#  ORDERS
#  GET  /orders/
#  GET  /orders/{id}
#  PATCH /orders/{id}/status
# ═══════════════════════════════════════════════════════════

@app.get("/orders/", response_model=List[schemas.OrderOut], tags=["Orders"])
def get_orders(
    status: Optional[str] = None,
    admin: models.Admin = Depends(get_current_admin),
    db: Session = Depends(get_db)
):
    orders = crud.get_all_orders(db, status)
    return [schemas.OrderOut.from_db(o) for o in orders]


@app.get("/orders/{order_id}", response_model=schemas.OrderOut, tags=["Orders"])
def get_order_detail(
    order_id: int,
    admin: models.Admin = Depends(get_current_admin),
    db: Session = Depends(get_db)
):
    order = crud.get_order(db, order_id)
    if not order:
        raise HTTPException(status_code=404, detail="Pesanan tidak ditemukan")
    return schemas.OrderOut.from_db(order)


@app.patch("/orders/{order_id}/status", response_model=schemas.OrderOut, tags=["Orders"])
def update_order_status(
    order_id: int,
    data: schemas.OrderStatusUpdate,
    admin: models.Admin = Depends(get_current_admin),
    db: Session = Depends(get_db)
):
    order = crud.update_order_status(db, order_id, data.status)
    if not order:
        raise HTTPException(status_code=404, detail="Pesanan tidak ditemukan")
    # Auto-create payment saat approved
    if data.status.lower() == "approved":
        existing = crud.get_payment_by_order(db, order_id)
        if not existing:
            crud.create_payment(db, order_id, schemas.PaymentCreate())
    return schemas.OrderOut.from_db(order)


# ═══════════════════════════════════════════════════════════
#  MENU
#  GET   /menu/
#  POST  /menu/          (multipart/form-data)
#  PUT   /menu/{id}      (multipart/form-data)
#  PATCH /menu/{id}/availability
#  DELETE /menu/{id}
# ═══════════════════════════════════════════════════════════

@app.get("/menu/", response_model=List[schemas.MenuOut], tags=["Menu"])
def get_menu(
    kategori: Optional[str] = None,
    admin: models.Admin = Depends(get_current_admin),
    db: Session = Depends(get_db)
):
    # Android kirim filter sebagai 'kategori', DB simpan sebagai 'category'
    menus = crud.get_all_menus(db, category=kategori)
    return [schemas.MenuOut.from_db(m) for m in menus]


@app.post("/menu/", response_model=schemas.MenuOut, status_code=201, tags=["Menu"])
async def create_menu(
    name:         str  = Form(...),
    price:        int  = Form(...),
    kategori:     str  = Form("makanan"),
    description:  str  = Form(""),
    is_available: str  = Form("true"),
    image:        Optional[UploadFile] = File(None),
    admin: models.Admin = Depends(get_current_admin),
    db: Session = Depends(get_db)
):
    image_url = None
    if image and image.filename:
        # Simpan ke folder static (opsional), atau langsung simpan filename saja
        image_url = f"/static/menu/{image.filename}"

    data = schemas.MenuCreate(
        name=name,
        price=price,
        kategori=kategori,
        description=description,
        is_available=is_available.lower() not in ("false", "0"),
        image_url=image_url,
    )
    # Sesuaikan dengan DB field 'category'
    menu_data = data.model_dump()
    menu_data["category"] = menu_data.pop("kategori")
    menu_data["image_url"] = menu_data.pop("image_url")

    import schemas as s
    db_data = s.MenuCreate(
        name=menu_data["name"], price=menu_data["price"],
        description=menu_data["description"],
        image_url=menu_data.get("image_url"),
        kategori=menu_data["category"],
        is_available=menu_data["is_available"],
    )

    # Patch crud untuk accept category field
    from models import Menu as MenuModel
    menu = MenuModel(
        name=db_data.name,
        description=db_data.description,
        price=db_data.price,
        category=db_data.kategori,
        image_url=db_data.image_url,
        is_available=db_data.is_available,
    )
    db.add(menu)
    db.commit()
    db.refresh(menu)
    return schemas.MenuOut.from_db(menu)


@app.put("/menu/{menu_id}", response_model=schemas.MenuOut, tags=["Menu"])
async def update_menu(
    menu_id: int,
    name:         str  = Form(...),
    price:        int  = Form(...),
    kategori:     str  = Form("makanan"),
    description:  str  = Form(""),
    is_available: str  = Form("true"),
    image:        Optional[UploadFile] = File(None),
    admin: models.Admin = Depends(get_current_admin),
    db: Session = Depends(get_db)
):
    menu = crud.get_menu(db, menu_id)
    if not menu:
        raise HTTPException(status_code=404, detail="Menu tidak ditemukan")

    menu.name        = name
    menu.price       = price
    menu.category    = kategori          # DB pakai 'category'
    menu.description = description
    menu.is_available= is_available.lower() not in ("false", "0")
    if image and image.filename:
        menu.image_url = f"/static/menu/{image.filename}"

    db.commit()
    db.refresh(menu)
    return schemas.MenuOut.from_db(menu)


@app.patch("/menu/{menu_id}/availability", response_model=schemas.MenuOut, tags=["Menu"])
def toggle_availability(
    menu_id: int,
    body: dict,
    admin: models.Admin = Depends(get_current_admin),
    db: Session = Depends(get_db)
):
    menu = crud.get_menu(db, menu_id)
    if not menu:
        raise HTTPException(status_code=404, detail="Menu tidak ditemukan")
    if "is_available" in body:
        menu.is_available = bool(body["is_available"])
        db.commit()
        db.refresh(menu)
    return schemas.MenuOut.from_db(menu)


@app.delete("/menu/{menu_id}", response_model=schemas.MessageResponse, tags=["Menu"])
def delete_menu(
    menu_id: int,
    admin: models.Admin = Depends(get_current_admin),
    db: Session = Depends(get_db)
):
    crud.delete_menu(db, menu_id)
    return {"message": "Menu berhasil dihapus"}


# ═══════════════════════════════════════════════════════════
#  PAYMENTS
#  GET /payments/
# ═══════════════════════════════════════════════════════════

@app.get("/payments/", response_model=List[schemas.PaymentOut], tags=["Payments"])
def get_payments(
    admin: models.Admin = Depends(get_current_admin),
    db: Session = Depends(get_db)
):
    payments = db.query(models.Payment).order_by(models.Payment.created_at.desc()).all()
    result = []
    for p in payments:
        order = crud.get_order(db, p.order_id)
        result.append(schemas.PaymentOut.from_db(p, order))
    return result


# ═══════════════════════════════════════════════════════════
#  ORDER — publik (untuk app pembeli, tidak perlu login)
# ═══════════════════════════════════════════════════════════

@app.post("/api/v1/orders", tags=["Public - Orders"], status_code=201)
def create_order_public(data: schemas.OrderCreate, db: Session = Depends(get_db)):
    order = crud.create_order(db, data)
    return schemas.OrderOut.from_db(order)


# ═══════════════════════════════════════════════════════════
#  CATEGORIES (tetap ada untuk kelengkapan)
# ═══════════════════════════════════════════════════════════

@app.get("/api/v1/categories", tags=["Categories"])
def get_categories(db: Session = Depends(get_db)):
    return crud.get_all_categories(db)


# ═══════════════════════════════════════════════════════════
#  HEALTH
# ═══════════════════════════════════════════════════════════

@app.get("/", tags=["Health"])
def root():
    return {
        "app": "NusaRasa API v2",
        "status": "running",
        "docs": "/docs",
        "endpoints_android": {
            "login":           "POST /auth/login",
            "dashboard":       "GET  /dashboard/stats",
            "orders":          "GET  /orders/",
            "order_detail":    "GET  /orders/{id}",
            "update_status":   "PATCH /orders/{id}/status",
            "menu":            "GET  /menu/",
            "create_menu":     "POST /menu/",
            "update_menu":     "PUT  /menu/{id}",
            "toggle_menu":     "PATCH /menu/{id}/availability",
            "delete_menu":     "DELETE /menu/{id}",
            "payments":        "GET  /payments/",
        }
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
