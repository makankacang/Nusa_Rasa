from pydantic import BaseModel, field_validator
from typing import List, Optional
from datetime import datetime


# ─────────────────────────────────────────
# AUTH
# ─────────────────────────────────────────
class AdminRegister(BaseModel):
    name: str
    email: str
    password: str

class AdminLogin(BaseModel):
    email: str
    password: str

class AdminOut(BaseModel):
    id: int
    name: str
    email: str

    class Config:
        from_attributes = True

# Android expect: { "token": "...", "admin": { "id":1, "name":"...", "email":"..." } }
class TokenResponse(BaseModel):
    token: str
    admin: AdminOut


# ─────────────────────────────────────────
# CATEGORY
# ─────────────────────────────────────────
class CategoryCreate(BaseModel):
    name: str

class CategoryOut(BaseModel):
    id: int
    name: str

    class Config:
        from_attributes = True


# ─────────────────────────────────────────
# MENU
# Android expect: id, name, price, kategori, description, is_available, image_url
# DB column     : id, name, price, category, description, is_available, image_url
# ─────────────────────────────────────────
class MenuCreate(BaseModel):
    name: str
    description: Optional[str] = None
    price: int
    image_url: Optional[str] = None   # Android kirim sebagai image_url
    kategori: Optional[str] = None    # Android kirim sebagai kategori
    is_available: bool = True

class MenuOut(BaseModel):
    id: int
    name: str
    description: Optional[str]
    price: int
    kategori: Optional[str] = None    # mapped dari DB column 'category'
    image_url: Optional[str] = None   # mapped dari DB column 'image_url'
    is_available: bool

    class Config:
        from_attributes = True

    @classmethod
    def from_db(cls, menu) -> "MenuOut":
        return cls(
            id=menu.id,
            name=menu.name,
            description=menu.description,
            price=int(menu.price),
            kategori=menu.category,       # DB: category → Android: kategori
            image_url=menu.image_url,
            is_available=menu.is_available,
        )


# ─────────────────────────────────────────
# ORDER
# Android expect: id, order_code, customer_name, table_number,
#                 items[]{menu_id, menu_name, quantity, price, subtotal},
#                 total, status, note, created_at
# DB columns    : id, customer_name, table_number, notes, total_price, status, created_at
#                 order_items[]{menu_id, quantity, subtotal, menu{name,price}}
# ─────────────────────────────────────────
class OrderItemCreate(BaseModel):
    menu_id: int
    quantity: int
    subtotal: int

class OrderItemOut(BaseModel):
    menu_id: int
    menu_name: str
    quantity: int
    price: int
    subtotal: int

    class Config:
        from_attributes = True

    @classmethod
    def from_db(cls, item) -> "OrderItemOut":
        return cls(
            menu_id=item.menu_id,
            menu_name=item.menu.name if item.menu else "Unknown",
            quantity=item.quantity,
            price=int(item.menu.price) if item.menu else 0,
            subtotal=int(item.subtotal),
        )

class OrderCreate(BaseModel):
    customer_name: str
    table_number: Optional[str] = None
    notes: Optional[str] = None
    items: List[OrderItemCreate]

class OrderStatusUpdate(BaseModel):
    status: str

class OrderOut(BaseModel):
    id: int
    order_code: str
    customer_name: str
    table_number: Optional[str]
    items: List[OrderItemOut]
    total: int
    status: str
    note: Optional[str]
    created_at: str

    @classmethod
    def from_db(cls, order) -> "OrderOut":
        return cls(
            id=order.id,
            order_code=f"ORD-{order.id:03d}",          # generate dari id
            customer_name=order.customer_name,
            table_number=order.table_number or "-",
            items=[OrderItemOut.from_db(i) for i in order.order_items],
            total=int(order.total_price),
            status=order.status.value if hasattr(order.status, 'value') else str(order.status),
            note=order.notes,
            created_at=order.created_at.isoformat() if order.created_at else "",
        )


# ─────────────────────────────────────────
# PAYMENT
# Android expect: id, order_id, order_code, buyer_name, amount, status, created_at
# DB columns    : id, order_id, payment_status, qris_code, paid_at, created_at
# ─────────────────────────────────────────
class PaymentCreate(BaseModel):
    payment_method: Optional[str] = None

class PaymentOut(BaseModel):
    id: int
    order_id: int
    order_code: str
    buyer_name: str
    amount: int
    status: str
    created_at: str

    @classmethod
    def from_db(cls, payment, order) -> "PaymentOut":
        status = payment.payment_status
        if hasattr(status, 'value'):
            status = status.value
        return cls(
            id=payment.id,
            order_id=payment.order_id,
            order_code=f"ORD-{payment.order_id:03d}",
            buyer_name=order.customer_name if order else "-",
            amount=int(order.total_price) if order else 0,
            status=status,
            created_at=payment.created_at.isoformat() if payment.created_at else "",
        )


# ─────────────────────────────────────────
# DASHBOARD
# Android expect: total_orders, pending, approved, paid, done,
#                 revenue, revenue_transactions, recent_orders
# ─────────────────────────────────────────
class DashboardStats(BaseModel):
    total_orders: int
    pending: int
    approved: int
    paid: int
    done: int
    revenue: int
    revenue_transactions: int
    recent_orders: List[OrderOut]


# ─────────────────────────────────────────
# GENERIC
# ─────────────────────────────────────────
class MessageResponse(BaseModel):
    message: str
