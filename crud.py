from sqlalchemy.orm import Session
from datetime import datetime
from typing import Optional
import models, schemas
from auth import hash_password, verify_password


# ─────────────────────────────────────────
# ADMIN CRUD
# ─────────────────────────────────────────
def get_admin_by_email(db: Session, email: str):
    return db.query(models.Admin).filter(models.Admin.email == email).first()

def create_admin(db: Session, data: schemas.AdminRegister):
    admin = models.Admin(
        name=data.name,
        email=data.email,
        password=hash_password(data.password)
    )
    db.add(admin)
    db.commit()
    db.refresh(admin)
    return admin

def authenticate_admin(db: Session, email: str, password: str):
    admin = get_admin_by_email(db, email)
    if not admin or not verify_password(password, admin.password):
        return None
    return admin


# ─────────────────────────────────────────
# CATEGORY CRUD
# ─────────────────────────────────────────
def get_all_categories(db: Session):
    return db.query(models.Category).all()

def create_category(db: Session, data: schemas.CategoryCreate):
    cat = models.Category(name=data.name)
    db.add(cat)
    db.commit()
    db.refresh(cat)
    return cat

def delete_category(db: Session, category_id: int):
    cat = db.query(models.Category).filter(models.Category.id == category_id).first()
    if cat:
        db.delete(cat)
        db.commit()


# ─────────────────────────────────────────
# MENU CRUD
# ─────────────────────────────────────────
def get_all_menus(db: Session, category: Optional[str] = None):
    query = db.query(models.Menu).filter(models.Menu.is_available == True)
    if category:
        query = query.filter(models.Menu.category == category)
    return query.all()

def get_menu(db: Session, menu_id: int):
    return db.query(models.Menu).filter(models.Menu.id == menu_id).first()

def create_menu(db: Session, data: schemas.MenuCreate):
    menu = models.Menu(**data.model_dump())
    db.add(menu)
    db.commit()
    db.refresh(menu)
    return menu

def update_menu(db: Session, menu_id: int, data: schemas.MenuCreate):
    menu = get_menu(db, menu_id)
    if not menu:
        return None
    for key, value in data.model_dump().items():
        setattr(menu, key, value)
    db.commit()
    db.refresh(menu)
    return menu

def delete_menu(db: Session, menu_id: int):
    menu = get_menu(db, menu_id)
    if menu:
        db.delete(menu)
        db.commit()


# ─────────────────────────────────────────
# ORDER CRUD
# ─────────────────────────────────────────
def create_order(db: Session, data: schemas.OrderCreate):
    total_price = sum(item.subtotal for item in data.items)

    order = models.Order(
        customer_name=data.customer_name,
        table_number=data.table_number,
        notes=data.notes,
        total_price=total_price,
        status=models.StatusOrder.PENDING
    )
    db.add(order)
    db.commit()
    db.refresh(order)

    for item in data.items:
        db_item = models.OrderItem(
            order_id=order.id,
            menu_id=item.menu_id,
            quantity=item.quantity,
            subtotal=item.subtotal
        )
        db.add(db_item)

    _add_log(db, order.id, "PENDING")
    db.commit()
    db.refresh(order)
    return order

def get_all_orders(db: Session, status: Optional[str] = None):
    query = db.query(models.Order)
    if status:
        query = query.filter(models.Order.status == status)
    return query.order_by(models.Order.created_at.desc()).all()

def get_order(db: Session, order_id: int):
    return db.query(models.Order).filter(models.Order.id == order_id).first()

def update_order_status(db: Session, order_id: int, status: str):
    order = get_order(db, order_id)
    if not order:
        return None
    order.status = status
    _add_log(db, order_id, status)
    db.commit()
    db.refresh(order)
    return order

def _add_log(db: Session, order_id: int, status: str):
    log = models.OrderLog(order_id=order_id, status=status)
    db.add(log)


# ─────────────────────────────────────────
# PAYMENT CRUD
# ─────────────────────────────────────────
def create_payment(db: Session, order_id: int, data: schemas.PaymentCreate):
    order = get_order(db, order_id)
    if not order:
        return None
    qris = f"QRIS-NUSARASA-{order_id:06d}-{order.total_price}"
    payment = models.Payment(
        order_id=order_id,
        payment_method=data.payment_method,
        qris_code=qris,
        payment_status=models.StatusPayment.UNPAID
    )
    db.add(payment)
    _add_log(db, order_id, "QRIS_GENERATED")
    db.commit()
    db.refresh(payment)
    return payment

def get_payment_by_order(db: Session, order_id: int):
    return db.query(models.Payment).filter(models.Payment.order_id == order_id).first()

def confirm_payment(db: Session, payment_id: int):
    payment = db.query(models.Payment).filter(models.Payment.id == payment_id).first()
    if not payment:
        return None
    payment.payment_status = models.StatusPayment.PAID
    payment.paid_at = datetime.now()
    order = get_order(db, payment.order_id)
    if order:
        order.status = models.StatusOrder.PAID
        _add_log(db, order.id, "PAID")
    db.commit()
    db.refresh(payment)
    return payment
