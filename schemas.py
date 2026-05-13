from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime


# ─────────────────────────────────────────
# AUTH SCHEMAS
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
    created_at: datetime

    class Config:
        from_attributes = True

class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    admin_id: int
    name: str


# ─────────────────────────────────────────
# CATEGORY SCHEMAS
# ─────────────────────────────────────────
class CategoryCreate(BaseModel):
    name: str

class CategoryOut(BaseModel):
    id: int
    name: str

    class Config:
        from_attributes = True


# ─────────────────────────────────────────
# MENU SCHEMAS
# ─────────────────────────────────────────
class MenuCreate(BaseModel):
    name: str
    description: Optional[str] = None
    price: int
    image: Optional[str] = None
    category: Optional[str] = None
    is_available: bool = True

class MenuOut(BaseModel):
    id: int
    name: str
    description: Optional[str]
    price: int
    image: Optional[str]
    category: Optional[str]
    is_available: bool

    class Config:
        from_attributes = True


# ─────────────────────────────────────────
# ORDER SCHEMAS
# ─────────────────────────────────────────
class OrderItemCreate(BaseModel):
    menu_id: int
    quantity: int
    subtotal: int

class OrderItemOut(BaseModel):
    id: int
    menu_id: int
    quantity: int
    subtotal: int
    menu: Optional[MenuOut]

    class Config:
        from_attributes = True

class OrderCreate(BaseModel):
    customer_name: str
    table_number: Optional[str] = None
    notes: Optional[str] = None
    items: List[OrderItemCreate]

class OrderStatusUpdate(BaseModel):
    status: str

class OrderLogOut(BaseModel):
    id: int
    status: Optional[str]
    created_at: datetime

    class Config:
        from_attributes = True

class OrderOut(BaseModel):
    id: int
    customer_name: str
    table_number: Optional[str]
    total_price: int
    status: str
    notes: Optional[str]
    created_at: datetime
    order_items: List[OrderItemOut] = []
    payment: Optional["PaymentOut"] = None
    logs: List[OrderLogOut] = []

    class Config:
        from_attributes = True


# ─────────────────────────────────────────
# PAYMENT SCHEMAS
# ─────────────────────────────────────────
class PaymentCreate(BaseModel):
    payment_method: Optional[str] = None

class PaymentOut(BaseModel):
    id: int
    order_id: int
    payment_method: Optional[str]
    payment_status: str
    qris_code: Optional[str]
    paid_at: Optional[datetime]

    class Config:
        from_attributes = True


# ─────────────────────────────────────────
# GENERAL
# ─────────────────────────────────────────
class MessageResponse(BaseModel):
    message: str


OrderOut.model_rebuild()
