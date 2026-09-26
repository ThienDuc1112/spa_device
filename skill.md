You are a Principal Software Architect and Senior Full Stack Engineer.


Create a production-ready system consisting of:


1. Spring Boot Backend (Java 21)

2. Android Application (Java)

3. Firebase Cloud Messaging (FCM)


The project is for a retail store PDA system.


=================================================

BUSINESS FEATURES

=================================================


FEATURE 1: PDA FINDER ALERT


Problem:

Store employees frequently lose PDA devices in the store.


Requirements:


1. User can search a PDA from the management screen.

2. System validates:

   - User permission

   - PDA belongs to current store

3. Create PDA search request.

4. Backend sends push notification to target PDA.

5. Android PDA receives notification and:

   - Displays popup

   - Starts Foreground Service

   - Plays alarm sound

   - Sets STREAM_ALARM volume to maximum

6. User can stop alarm via button.

7. Alarm automatically stops after configurable timeout.

8. Log all operations.

9. Handle offline devices.

10. Handle invalid FCM tokens.

11. Support token refresh.


=================================================

FEATURE 2: PRODUCT IMAGE LOOKUP

=================================================


Requirements:


1. User scans barcode using PDA scanner.

2. Android calls product lookup API.

3. Backend returns:


{

    "barcode": "",

    "productCode": "",

    "productName": "",

    "imageUrl": ""

}


4. Android displays product image.

5. Support image zoom.

6. Handle:

   - imageUrl null

   - image load failure

   - network failure

7. Display placeholder image.

8. Build image synchronization architecture:

   ERP -> Product Database -> Image Server


=================================================

FEATURE 3: INVENTORY ADJUSTMENT LINK

=================================================


Requirements:


1. Add Inventory Adjustment button on Scan Order screen.

2. Show button for selected product.

3. Pass current product code.

4. Navigate to Inventory Adjustment screen.

5. Load inventory information.

6. Save inventory adjustment.

7. Refresh inventory after save.

8. Preserve Scan Order state.

9. Do not reload all scanned items.

10. Use efficient UI state management.


=================================================

FEATURE 4: DISPOSAL MANAGEMENT

=================================================


Requirements:


1. Disposal inquiry screen.

2. Disposal detail screen.

3. Confirm disposal.

4. Cancel disposal.

5. Inventory deduction.

6. Transaction history.

7. Inventory synchronization.

8. Quantity validation.

9. Complete audit log.


=================================================

BACKEND REQUIREMENTS

=================================================


Use:


- Java 21

- Spring Boot 3.x

- Spring Security

- JWT Access Token

- JWT Refresh Token

- MyBatis

- PostgreSQL

- Flyway

- Maven

- Lombok

- MapStruct

- Firebase Admin SDK


Architecture:


Clean Architecture


Structure:


com.company

├── domain

├── application

├── infrastructure

├── presentation


Each feature must contain:


- Controller

- UseCase

- Service

- Repository

- Mapper

- DTO

- Entity


Implement:


- Global Exception Handler

- Validation

- Audit Fields

- Logging

- Security

- Unit Tests


=================================================

ANDROID REQUIREMENTS

=================================================


Use:


- Java

- MVVM

- Retrofit

- OkHttp

- Glide

- Room

- Firebase Messaging

- Data Binding


Architecture:


presentation

domain

data


Implement:


- Login

- JWT Storage

- Auto Refresh Token

- Barcode Scan Flow

- PDA Finder Flow

- Product Image Viewer

- Inventory Flow

- Disposal Flow


Handle:


- Offline Mode

- Retry

- Error Dialog

- Loading Dialog


=================================================

DATABASE DESIGN

=================================================


Generate complete schema:


users

roles

user_roles


stores


devices


pda_find_requests


pda_alert_logs


products


product_image_sync_logs


inventories


inventory_adjustments


inventory_transactions


disposals


disposal_items


disposal_histories


Include:


- PK

- FK

- Index

- Unique Constraint


Generate Flyway migration scripts.


=================================================

API DESIGN

=================================================


Generate REST APIs:


Authentication


POST /auth/login

POST /auth/refresh


Device


POST /devices/register

PUT /devices/token


PDA Finder


POST /pda/find

POST /pda/stop


Product


GET /products/barcode/{barcode}


Inventory


POST /inventory-adjustments


Disposal


GET /disposals

POST /disposals

POST /disposals/{id}/confirm


Generate:


- Request DTO

- Response DTO

- Validation Rules

- Example JSON


=================================================

NON FUNCTIONAL REQUIREMENTS

=================================================


- Clean Code

- SOLID

- Production Ready

- Transaction Safety

- Optimistic Locking

- Audit Logging

- Secure JWT Handling

- Proper Exception Handling

- Scalable Design


=================================================

OUTPUT FORMAT

=================================================


Generate in the following order:


1. System Architecture Diagram

2. Database ERD

3. Backend Project Structure

4. Android Project Structure

5. Flyway SQL Scripts

6. Entity Models

7. API Contracts

8. Backend Source Code

9. Android Source Code

10. Sequence Diagrams

11. Deployment Guide

12. Future Improvements


Do not provide high-level explanations only.


Provide complete production-quality source code and implementation details.
 
Phase 1: Generate ERD + Flyway.
Phase 2: Generate Spring Boot Authentication.
Phase 3: Generate PDA Finder Module.
Phase 4: Generate Product Image Module.
Phase 5: Generate Inventory Module.
Phase 6: Generate Disposal Module.
Phase 7: Generate Android App.
Phase 8: Integration Test.
 
-- =========================

-- STORE

-- =========================


CREATE TABLE stores (

    id BIGSERIAL PRIMARY KEY,

    store_code VARCHAR(50) UNIQUE NOT NULL,

    store_name VARCHAR(255) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);


-- =========================

-- USERS

-- =========================


CREATE TABLE users (

    id BIGSERIAL PRIMARY KEY,

    username VARCHAR(100) UNIQUE NOT NULL,

    password_hash VARCHAR(255) NOT NULL,

    full_name VARCHAR(255),

    email VARCHAR(255),

    store_id BIGINT,

    active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_user_store

        FOREIGN KEY(store_id)

        REFERENCES stores(id)

);


-- =========================

-- ROLE

-- =========================


CREATE TABLE roles (

    id BIGSERIAL PRIMARY KEY,

    role_name VARCHAR(50) UNIQUE NOT NULL,

    description VARCHAR(255)

);


CREATE TABLE user_roles (

    user_id BIGINT NOT NULL,

    role_id BIGINT NOT NULL,


    PRIMARY KEY(user_id, role_id),


    CONSTRAINT fk_ur_user

        FOREIGN KEY(user_id)

        REFERENCES users(id),


    CONSTRAINT fk_ur_role

        FOREIGN KEY(role_id)

        REFERENCES roles(id)

);


-- =========================

-- DEVICE / PDA

-- =========================


CREATE TABLE devices (

    id BIGSERIAL PRIMARY KEY,

    device_code VARCHAR(100) UNIQUE NOT NULL,

    device_name VARCHAR(255),

    store_id BIGINT NOT NULL,


    fcm_token TEXT,


    status VARCHAR(20) DEFAULT 'ONLINE',


    last_active_at TIMESTAMP,


    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_device_store

        FOREIGN KEY(store_id)

        REFERENCES stores(id)

);


CREATE INDEX idx_device_code

ON devices(device_code);


-- =========================

-- PDA FINDER

-- =========================


CREATE TABLE pda_find_requests (

    id BIGSERIAL PRIMARY KEY,


    request_no VARCHAR(50) UNIQUE,


    requester_id BIGINT NOT NULL,


    device_id BIGINT NOT NULL,


    status VARCHAR(20) NOT NULL,


    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_find_user

        FOREIGN KEY(requester_id)

        REFERENCES users(id),


    CONSTRAINT fk_find_device

        FOREIGN KEY(device_id)

        REFERENCES devices(id)

);


CREATE TABLE pda_alert_logs (

    id BIGSERIAL PRIMARY KEY,


    request_id BIGINT NOT NULL,

    device_id BIGINT NOT NULL,


    send_status VARCHAR(20),


    error_message VARCHAR(500),


    sent_time TIMESTAMP,


    CONSTRAINT fk_log_request

        FOREIGN KEY(request_id)

        REFERENCES pda_find_requests(id),


    CONSTRAINT fk_log_device

        FOREIGN KEY(device_id)

        REFERENCES devices(id)

);


-- =========================

-- PRODUCT

-- =========================


CREATE TABLE products (

    id BIGSERIAL PRIMARY KEY,


    product_code VARCHAR(50) UNIQUE NOT NULL,

    barcode VARCHAR(50) UNIQUE NOT NULL,


    product_name VARCHAR(255) NOT NULL,


    image_url VARCHAR(1000),


    unit_price NUMERIC(18,2),


    active BOOLEAN DEFAULT TRUE,


    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);


CREATE INDEX idx_product_barcode

ON products(barcode);


CREATE TABLE product_image_sync_logs (

    id BIGSERIAL PRIMARY KEY,


    product_id BIGINT NOT NULL,


    image_url VARCHAR(1000),


    sync_status VARCHAR(20),


    message VARCHAR(1000),


    synced_at TIMESTAMP,


    CONSTRAINT fk_image_sync_product

        FOREIGN KEY(product_id)

        REFERENCES products(id)

);


-- =========================

-- INVENTORY

-- =========================


CREATE TABLE inventories (

    id BIGSERIAL PRIMARY KEY,


    product_id BIGINT NOT NULL,

    store_id BIGINT NOT NULL,


    quantity NUMERIC(18,2) DEFAULT 0,


    version BIGINT DEFAULT 0,


    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT uk_inventory

        UNIQUE(product_id, store_id),


    CONSTRAINT fk_inventory_product

        FOREIGN KEY(product_id)

        REFERENCES products(id),


    CONSTRAINT fk_inventory_store

        FOREIGN KEY(store_id)

        REFERENCES stores(id)

);


CREATE TABLE inventory_adjustments (

    id BIGSERIAL PRIMARY KEY,


    adjustment_no VARCHAR(50) UNIQUE,


    product_id BIGINT NOT NULL,


    store_id BIGINT NOT NULL,


    old_qty NUMERIC(18,2),


    new_qty NUMERIC(18,2),


    reason VARCHAR(1000),


    created_by BIGINT,


    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_adj_product

        FOREIGN KEY(product_id)

        REFERENCES products(id),


    CONSTRAINT fk_adj_store

        FOREIGN KEY(store_id)

        REFERENCES stores(id),


    CONSTRAINT fk_adj_user

        FOREIGN KEY(created_by)

        REFERENCES users(id)

);


CREATE TABLE inventory_transactions (

    id BIGSERIAL PRIMARY KEY,


    transaction_no VARCHAR(50) UNIQUE,


    product_id BIGINT NOT NULL,

    store_id BIGINT NOT NULL,


    transaction_type VARCHAR(30) NOT NULL,


    qty_before NUMERIC(18,2),

    qty_change NUMERIC(18,2),

    qty_after NUMERIC(18,2),


    reference_id BIGINT,


    created_by BIGINT,


    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);


-- =========================

-- DISPOSAL

-- =========================


CREATE TABLE disposals (

    id BIGSERIAL PRIMARY KEY,


    disposal_no VARCHAR(50) UNIQUE,


    store_id BIGINT NOT NULL,


    status VARCHAR(20) DEFAULT 'PENDING',


    remarks VARCHAR(1000),


    created_by BIGINT NOT NULL,


    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_disposal_store

        FOREIGN KEY(store_id)

        REFERENCES stores(id),


    CONSTRAINT fk_disposal_user

        FOREIGN KEY(created_by)

        REFERENCES users(id)

);


CREATE TABLE disposal_items (

    id BIGSERIAL PRIMARY KEY,


    disposal_id BIGINT NOT NULL,


    product_id BIGINT NOT NULL,


    quantity NUMERIC(18,2) NOT NULL,


    reason VARCHAR(500),


    CONSTRAINT fk_disposal_item_header

        FOREIGN KEY(disposal_id)

        REFERENCES disposals(id)

        ON DELETE CASCADE,


    CONSTRAINT fk_disposal_item_product

        FOREIGN KEY(product_id)

        REFERENCES products(id)

);


CREATE TABLE disposal_histories (

    id BIGSERIAL PRIMARY KEY,


    disposal_id BIGINT NOT NULL,


    old_status VARCHAR(20),


    new_status VARCHAR(20),


    changed_by BIGINT,


    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_disposal_history

        FOREIGN KEY(disposal_id)

        REFERENCES disposals(id)

);

 scanner/
│
├── api/
│   ├── ScannerManager.java
│   ├── ScanResult.java
│   ├── ScanCallback.java
│   ├── ScannerConfig.java
│   └── ScannerType.java
│
├── factory/
│   └── ScannerFactory.java
│
├── zebra/
│   ├── ZebraScannerManager.java
│   ├── ZebraScannerConfig.java
│   └── emdk/
│
├── urovo/
│   ├── UrovoScannerManager.java
│   ├── UrovoScannerConfig.java
│   └── sdk/
│
├── honeywell/
│   ├── HoneywellScannerManager.java
│   └── sdk/
│
├── mock/
│   └── MockScannerManager.java
│
└── di/
    └── ScannerModule.javaa