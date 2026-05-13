from sqlalchemy import Column, Integer, String, Boolean, DateTime, ForeignKey, Text, Enum
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from database import Base
import enum


class Admin(Base):
    __tablename__ = "admins"

    id         = Column(Integer, primary_key=True, index=True, autoincrement=True)
    name       = Column(String(100), nullable=False)
    email      = Column(String(100), unique=True, nullable=False)
    password   = Column(String(255), nullable=False)
    created_at = Column(DateTime, server_default=func.now())


class Category(Base):
    __tablename__ = "categories"

    id   = Column(Integer, primary_key=True, index=True, autoincrement=True)
    name = Column(String(100), nullable=False)


class Menu(Base):
    __tablename__ = "menus"

    id           = Column(Integer, primary_key=True, index=True, autoincrement=True)
    name         = Column(String(100), nullable=False)
    description  = Column(Text, nullable=True)
    price        = Column(Integer, nullable=False)
    image        = Column(String(255), nullable=True)
    category     = Column(String(100), nullable=True)
    is_available = Column(Boolean, default=True)
    created_at   = Column(DateTime, server_default=func.now())

    order_items = relationship("OrderItem", back_populates="menu", lazy="selectin")


class StatusOrder(str, enum.Enum):
    PENDING  = "PENDING"
    APPROVED = "APPROVED"
    PAID     = "PAID"
    DONE     = "DONE"
    REJECTED = "REJECTED"


class Order(Base):
    __tablename__ = "orders"

    id            = Column(Integer, primary_key=True, index=True, autoincrement=True)
    customer_name = Column(String(100), nullable=False)
    table_number  = Column(String(20), nullable=True)
    total_price   = Column(Integer, nullable=False)
    status        = Column(Enum(StatusOrder), default=StatusOrder.PENDING)
    notes         = Column(Text, nullable=True)
    created_at    = Column(DateTime, server_default=func.now())

    order_items = relationship("OrderItem", back_populates="order", lazy="selectin")
    payment     = relationship("Payment", back_populates="order", uselist=False, lazy="selectin")
    logs        = relationship("OrderLog", back_populates="order", lazy="selectin")


class OrderItem(Base):
    __tablename__ = "order_items"

    id       = Column(Integer, primary_key=True, index=True, autoincrement=True)
    order_id = Column(Integer, ForeignKey("orders.id", ondelete="CASCADE"), nullable=False)
    menu_id  = Column(Integer, ForeignKey("menus.id", ondelete="CASCADE"), nullable=False)
    quantity = Column(Integer, nullable=False)
    subtotal = Column(Integer, nullable=False)

    order = relationship("Order", back_populates="order_items")
    menu  = relationship("Menu", back_populates="order_items")


class StatusPayment(str, enum.Enum):
    UNPAID = "UNPAID"
    PAID   = "PAID"
    FAILED = "FAILED"


class Payment(Base):
    __tablename__ = "payments"

    id             = Column(Integer, primary_key=True, index=True, autoincrement=True)
    order_id       = Column(Integer, ForeignKey("orders.id", ondelete="CASCADE"), nullable=False)
    payment_method = Column(String(50), nullable=True)
    payment_status = Column(Enum(StatusPayment), default=StatusPayment.UNPAID)
    qris_code      = Column(String(255), nullable=True)
    paid_at        = Column(DateTime, nullable=True)

    order = relationship("Order", back_populates="payment")


class OrderLog(Base):
    __tablename__ = "order_logs"

    id         = Column(Integer, primary_key=True, index=True, autoincrement=True)
    order_id   = Column(Integer, ForeignKey("orders.id", ondelete="CASCADE"), nullable=False)
    status     = Column(String(50), nullable=True)
    created_at = Column(DateTime, server_default=func.now())

    order = relationship("Order", back_populates="logs")
