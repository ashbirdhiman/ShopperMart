# # Script to populate all ShopperMart service databases with test data
# # Runs INSERT commands directly for user, order, and inventory services
#
# Write-Host "========================================" -ForegroundColor Cyan
# Write-Host "ShopperMart Database Population Script" -ForegroundColor Cyan
# Write-Host "========================================" -ForegroundColor Cyan
# Write-Host ""
#
# # User Service Data
# Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Populating user_service database..." -ForegroundColor Yellow
# $userCommand = @"
# docker exec user-service-mysql mysql -u root -pmysql user_service -e "INSERT INTO users (user_id, email, first_name, last_name, phone_number) VALUES ('550e8400-e29b-41d4-a716-446655440001', 'john.doe@shoppermart.com', 'John', 'Doe', '+1-555-0101'),('550e8400-e29b-41d4-a716-446655440002', 'jane.smith@shoppermart.com', 'Jane', 'Smith', '+1-555-0102'),('550e8400-e29b-41d4-a716-446655440003', 'mike.johnson@shoppermart.com', 'Mike', 'Johnson', '+1-555-0103'),('550e8400-e29b-41d4-a716-446655440004', 'sarah.williams@shoppermart.com', 'Sarah', 'Williams', '+1-555-0104'),('550e8400-e29b-41d4-a716-446655440005', 'david.brown@shoppermart.com', 'David', 'Brown', '+1-555-0105'),('550e8400-e29b-41d4-a716-446655440006', 'emily.davis@shoppermart.com', 'Emily', 'Davis', '+1-555-0106'),('550e8400-e29b-41d4-a716-446655440007', 'robert.miller@shoppermart.com', 'Robert', 'Miller', '+1-555-0107'),('550e8400-e29b-41d4-a716-446655440008', 'lisa.anderson@shoppermart.com', 'Lisa', 'Anderson', '+1-555-0108'),('550e8400-e29b-41d4-a716-446655440009', 'james.taylor@shoppermart.com', 'James', 'Taylor', '+1-555-0109'),('550e8400-e29b-41d4-a716-446655440010', 'patricia.thomas@shoppermart.com', 'Patricia', 'Thomas', '+1-555-0110');"
# "@
#
# try {
#     Invoke-Expression $userCommand 2>&1 | Out-Null
#     if ($LASTEXITCODE -eq 0) {
#         Write-Host "  Success: user_service populated" -ForegroundColor Green
#     } else {
#         Write-Host "  Warning: user_service completed with exit code $LASTEXITCODE" -ForegroundColor Yellow
#     }
# } catch {
#     Write-Host "  Error: $_" -ForegroundColor Red
# }
#
# Write-Host ""
#
# # Order Service Data
# Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Populating order_service database..." -ForegroundColor Yellow
# $orderCommand = @"
# docker exec order-service-mysql mysql -u root -pmysql order_service -e "INSERT INTO orders (customer_id, sku_code, quantity, total_price, status, created_at) VALUES ('550e8400-e29b-41d4-a716-446655440001', 'SKU-LAPTOP-001', 1, 999.99, 'CONFIRMED', '2024-01-15 11:20:00'),('550e8400-e29b-41d4-a716-446655440002', 'SKU-PHONE-001', 2, 2199.98, 'CONFIRMED', '2024-01-15 11:25:00'),('550e8400-e29b-41d4-a716-446655440003', 'SKU-TABLET-001', 1, 1299.99, 'PENDING', '2024-01-15 11:30:00'),('550e8400-e29b-41d4-a716-446655440004', 'SKU-MONITOR-001', 1, 799.99, 'CONFIRMED', '2024-01-15 11:35:00'),('550e8400-e29b-41d4-a716-446655440005', 'SKU-KEYBOARD-001', 2, 299.98, 'CONFIRMED', '2024-01-15 11:40:00'),('550e8400-e29b-41d4-a716-446655440001', 'SKU-MOUSE-001', 3, 299.97, 'PENDING', '2024-01-15 11:45:00'),('550e8400-e29b-41d4-a716-446655440002', 'SKU-HEADPHONE-001', 1, 399.99, 'CONFIRMED', '2024-01-15 11:50:00'),('550e8400-e29b-41d4-a716-446655440003', 'SKU-LAPTOP-002', 1, 599.99, 'CONFIRMED', '2024-01-15 11:55:00'),('550e8400-e29b-41d4-a716-446655440004', 'SKU-PHONE-002', 1, 899.99, 'PENDING', '2024-01-15 12:00:00'),('550e8400-e29b-41d4-a716-446655440005', 'SKU-CAMERA-001', 1, 1499.99, 'CONFIRMED', '2024-01-15 12:05:00'),('550e8400-e29b-41d4-a716-446655440006', 'SKU-MOUSE-002', 5, 99.95, 'CONFIRMED', '2024-01-15 12:10:00'),('550e8400-e29b-41d4-a716-446655440007', 'SKU-KEYBOARD-002', 2, 159.98, 'PENDING', '2024-01-15 12:15:00'),('550e8400-e29b-41d4-a716-446655440008', 'SKU-TABLET-002', 1, 799.99, 'CONFIRMED', '2024-01-15 12:20:00'),('550e8400-e29b-41d4-a716-446655440009', 'SKU-MONITOR-002', 2, 1199.98, 'CONFIRMED', '2024-01-15 12:25:00'),('550e8400-e29b-41d4-a716-446655440010', 'SKU-CAMERA-002', 1, 898.00, 'PENDING', '2024-01-15 12:30:00'),('550e8400-e29b-41d4-a716-446655440001', 'SKU-HEADPHONE-002', 2, 499.98, 'CONFIRMED', '2024-01-15 12:35:00'),('550e8400-e29b-41d4-a716-446655440002', 'SKU-PHONE-001', 1, 1099.99, 'CONFIRMED', '2024-01-15 12:40:00'),('550e8400-e29b-41d4-a716-446655440003', 'SKU-LAPTOP-001', 1, 999.99, 'PENDING', '2024-01-15 12:45:00'),('550e8400-e29b-41d4-a716-446655440004', 'SKU-KEYBOARD-001', 1, 149.99, 'CONFIRMED', '2024-01-15 12:50:00'),('550e8400-e29b-41d4-a716-446655440005', 'SKU-MOUSE-001', 2, 199.98, 'CONFIRMED', '2024-01-15 12:55:00');"
# "@
#
# try {
#     Invoke-Expression $orderCommand 2>&1 | Out-Null
#     if ($LASTEXITCODE -eq 0) {
#         Write-Host "  Success: order_service populated" -ForegroundColor Green
#     } else {
#         Write-Host "  Warning: order_service completed with exit code $LASTEXITCODE" -ForegroundColor Yellow
#     }
# } catch {
#     Write-Host "  Error: $_" -ForegroundColor Red
# }
#
# Write-Host ""

# Inventory Service Data
Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Populating inventory_service database..." -ForegroundColor Yellow
$inventoryCommand = @"
docker exec mysql mysql -u root -pmysql inventory_service -e "INSERT INTO inventory (sku_code, quantity) VALUES ('SKU-LAPTOP-001', 50),('SKU-LAPTOP-002', 35),('SKU-PHONE-001', 100),('SKU-PHONE-002', 75),('SKU-TABLET-001', 45),('SKU-TABLET-002', 60),('SKU-MONITOR-001', 25),('SKU-MONITOR-002', 30),('SKU-KEYBOARD-001', 120),('SKU-KEYBOARD-002', 110),('SKU-MOUSE-001', 200),('SKU-MOUSE-002', 180),('SKU-HEADPHONE-001', 80),('SKU-HEADPHONE-002', 70),('SKU-CAMERA-001', 20),('SKU-CAMERA-002', 15);"
"@

try {
    Invoke-Expression $inventoryCommand 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  Success: inventory_service populated" -ForegroundColor Green
    } else {
        Write-Host "  Warning: inventory_service completed with exit code $LASTEXITCODE" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  Error: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Database population completed!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
