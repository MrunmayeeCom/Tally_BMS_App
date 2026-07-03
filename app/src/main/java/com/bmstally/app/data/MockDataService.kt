package com.bmstally.app.data

import com.bmstally.app.model.*

private data class TenantData(
    val companies: List<Company>,
    val dashboardStats: List<DashboardStat>,
    val outstandingSummary: String,
    val outstandingLedgers: List<OutstandingItem>,
    val outstandingGroups: List<Pair<String, String>>,
    val items: List<Item>,
    val parties: List<String>
)

object MockDataService {
    init { seed() }

    private val stores = mutableMapOf<String, TenantData>()
    private val users = mutableMapOf<String, String>()
    private val tenants = mutableListOf<Tenant>()

    private fun register(
        id: String, code: String, name: String, gstin: String?,
        adminUser: String, adminPass: String, data: TenantData
    ) {
        tenants.add(Tenant(id, code, name, gstin))
        stores[id] = data
        users["$code:$adminUser"] = adminPass
    }

    fun seed() {
        if (tenants.isNotEmpty()) return

        register("t1", "tally", "Tally Solutions", "27AAACT1234A1Z5",
            "admin", "admin123", TenantData(
                companies = listOf(
                    Company("1", "Tally Solutions", "27AAACT1234A1Z5"),
                    Company("2", "BMS Corp", "27AABCS5678B1Z6")
                ),
                dashboardStats = listOf(
                    DashboardStat("Sales - Credit Note (Gross)", "₹ 12,37,41,37,96,173"),
                    DashboardStat("Purchase - Debit Note (Gross)", "₹ 10,61,34,78,07,790"),
                    DashboardStat("Receipt", "₹ 5,67,89,012"),
                    DashboardStat("Payment", "₹ 4,56,78,901"),
                    DashboardStat("Outstanding Receivable", "₹ 12,37,41,37,96,173"),
                    DashboardStat("Outstanding Payable", "₹ 10,61,34,78,07,790"),
                    DashboardStat("Cash / Bank Balance", "₹ 5,67,89,012"),
                    DashboardStat("Sales Order", "₹ 4,56,78,901"),
                    DashboardStat("Purchase Order", "₹ 3,45,67,890"),
                    DashboardStat("Delivery Note", "₹ 2,34,56,789"),
                    DashboardStat("Receipt Note", "₹ 1,23,45,678")
                ),
                outstandingSummary = "₹ 12,37,41,37,96,173",
                outstandingLedgers = listOf(
                    OutstandingItem("ABC Enterprises", "Cr", "₹ 1,23,45,678", true, "30 days", "Avg 28 days"),
                    OutstandingItem("XYZ Industries", "Dr", "₹ 56,78,901", false, "15 days", "Avg 14 days"),
                    OutstandingItem("PQR Tradelink", "Cr", "₹ 7,89,01,234", true, "60 days", "Avg 55 days"),
                    OutstandingItem("LMN Brothers", "Dr", "₹ 12,34,567", false, "45 days", "Avg 40 days"),
                    OutstandingItem("EFG Corporation", "Cr", "₹ 3,45,67,890", true, "90 days", "Avg 85 days")
                ),
                outstandingGroups = listOf("Sundry Debtors" to "₹ 12,37,41,37,96,173", "Sundry Creditors" to "₹ 10,61,34,78,07,790", "Loans & Advances" to "₹ 45,67,890"),
                items = listOf(
                    Item("1", "Cement 53 Grade", "Building Material", "Raw Material", "Bags", 250, 300, 350, 420, 50),
                    Item("2", "Steel Rods 12mm", "Building Material", "Raw Material", "Kg", 5000, 6000, 72, 85, 1000),
                    Item("3", "Bricks (Red)", "Building Material", "Raw Material", "Nos", 15000, 20000, 8, 12, 5000),
                    Item("4", "Paint - White 20L", "Paint", "Finished Good", "Pcs", 45, 60, 1800, 2400, 10),
                    Item("5", "Paint - Blue 10L", "Paint", "Finished Good", "Pcs", 30, 40, 1200, 1600, 8),
                    Item("6", "PVC Pipe 4 inch", "Plumbing", "Trading Good", "Pcs", 120, 150, 450, 580, 20),
                    Item("7", "PVC Pipe 2 inch", "Plumbing", "Trading Good", "Pcs", 200, 250, 280, 370, 30),
                    Item("8", "Tiles - Floor 2x2", "Tiles", "Finished Good", "Box", 80, 100, 650, 890, 15),
                    Item("9", "Tiles - Wall 1x1", "Tiles", "Finished Good", "Box", 60, 80, 520, 720, 10),
                    Item("10", "Sand (Fine)", "Building Material", "Raw Material", "Ton", 0, 20, 1200, 1600, 5),
                    Item("11", "Electrical Wire 1.5mm", "Electrical", "Trading Good", "Roll", 90, 120, 950, 1250, 15),
                    Item("12", "Switch Board 6 Module", "Electrical", "Trading Good", "Pcs", 150, 200, 180, 250, 25),
                    Item("13", "Water Tank 1000L", "Plumbing", "Trading Good", "Pcs", 12, 15, 4500, 6200, 3),
                    Item("14", "Sanitaryware - WC", "Sanitary", "Finished Good", "Pcs", 25, 30, 3200, 4500, 5),
                    Item("15", "Magnetic Tiles (Set)", "Tiles", "Finished Good", "Set", 0, 10, 2800, 3800, 2),
                    Item("16", "Adhesive - Tile Fix", "Building Material", "Consumable", "Kg", 300, 400, 45, 65, 50),
                    Item("17", "LED Panel Light 2x2", "Electrical", "Trading Good", "Pcs", 40, 50, 850, 1200, 8),
                    Item("18", "CPVC Pipe 1 inch", "Plumbing", "Trading Good", "Pcs", 0, 100, 180, 240, 20)
                ),
                parties = listOf("ABC Enterprises", "XYZ Industries", "PQR Tradelink", "LMN Brothers", "EFG Corporation", "Amit Traders", "Bharat Electronics")
            )
        )

        register("t2", "bmscorp", "BMS Corp", "27AABCS5678B1Z6",
            "admin", "admin456", TenantData(
                companies = listOf(
                    Company("1", "BMS Corp", "27AABCS5678B1Z6"),
                    Company("2", "BMS Retail", "27AABCR9012D1Z8")
                ),
                dashboardStats = listOf(
                    DashboardStat("Sales - Credit Note (Gross)", "₹ 1,200"),
                    DashboardStat("Purchase - Debit Note (Gross)", "₹ 500"),
                    DashboardStat("Receipt", "₹ 5,100"),
                    DashboardStat("Payment", "₹ 4,200"),
                    DashboardStat("Outstanding Receivable", "₹ 45,67,890"),
                    DashboardStat("Outstanding Payable", "₹ 32,10,987"),
                    DashboardStat("Cash / Bank Balance", "₹ 12,50,000"),
                    DashboardStat("Sales Order", "₹ 8,75,000"),
                    DashboardStat("Purchase Order", "₹ 5,20,000"),
                    DashboardStat("Delivery Note", "₹ 3,10,000"),
                    DashboardStat("Receipt Note", "₹ 2,45,000")
                ),
                outstandingSummary = "₹ 45,67,890",
                outstandingLedgers = listOf(
                    OutstandingItem("Amit Traders", "Cr", "₹ 12,500", true, "30 days", "Avg 25 days"),
                    OutstandingItem("Bharat Electronics", "Dr", "₹ 8,200", false, "15 days", "Avg 12 days"),
                    OutstandingItem("Crystal Distributors", "Cr", "₹ 25,000", true, "45 days", "Avg 40 days"),
                    OutstandingItem("Delta Supplies", "Dr", "₹ 5,100", false, "10 days", "Avg 8 days")
                ),
                outstandingGroups = listOf("Sundry Debtors" to "₹ 45,67,890", "Sundry Creditors" to "₹ 32,10,987", "Loans & Advances" to "₹ 12,50,000"),
                items = listOf(
                    Item("b1", "Office Chair", "Furniture", "Finished Good", "Pcs", 45, 50, 4500, 6500, 10),
                    Item("b2", "Standing Desk", "Furniture", "Finished Good", "Pcs", 20, 25, 12000, 18000, 5),
                    Item("b3", "LED Monitor 24\"", "Electronics", "Trading Good", "Pcs", 60, 80, 8500, 12000, 15),
                    Item("b4", "Wireless Keyboard", "Electronics", "Trading Good", "Pcs", 120, 150, 1200, 1800, 20),
                    Item("b5", "USB-C Hub", "Electronics", "Trading Good", "Pcs", 200, 250, 800, 1400, 30)
                ),
                parties = listOf("Amit Traders", "Bharat Electronics", "Crystal Distributors", "Delta Supplies")
            )
        )

        register("t3", "techmart", "TechMart India", "29AAACT9012C1Z7",
            "admin", "admin789", TenantData(
                companies = listOf(
                    Company("1", "TechMart India", "29AAACT9012C1Z7"),
                    Company("2", "TechMart Wholesale", "29AABCT3456E1Z0")
                ),
                dashboardStats = listOf(
                    DashboardStat("Sales - Credit Note (Gross)", "₹ 12,340"),
                    DashboardStat("Purchase - Debit Note (Gross)", "₹ 8,760"),
                    DashboardStat("Receipt", "₹ 98,765"),
                    DashboardStat("Payment", "₹ 76,543"),
                    DashboardStat("Outstanding Receivable", "₹ 2,34,56,789"),
                    DashboardStat("Outstanding Payable", "₹ 1,98,76,543"),
                    DashboardStat("Cash / Bank Balance", "₹ 56,78,900"),
                    DashboardStat("Sales Order", "₹ 34,56,789"),
                    DashboardStat("Purchase Order", "₹ 23,45,678"),
                    DashboardStat("Delivery Note", "₹ 12,34,567"),
                    DashboardStat("Receipt Note", "₹ 8,90,123")
                ),
                outstandingSummary = "₹ 2,34,56,789",
                outstandingLedgers = listOf(
                    OutstandingItem("Global Mart", "Cr", "₹ 1,25,000", true, "60 days", "Avg 55 days"),
                    OutstandingItem("Horizon Pvt Ltd", "Dr", "₹ 67,890", false, "20 days", "Avg 18 days"),
                    OutstandingItem("Innovative Tech", "Cr", "₹ 2,10,000", true, "90 days", "Avg 85 days")
                ),
                outstandingGroups = listOf("Sundry Debtors" to "₹ 2,34,56,789", "Sundry Creditors" to "₹ 1,98,76,543", "Loans & Advances" to "₹ 56,78,900"),
                items = listOf(
                    Item("t1", "Smartphone X Pro", "Mobile", "Finished Good", "Pcs", 150, 200, 18000, 25000, 25),
                    Item("t2", "Tablet Z10", "Mobile", "Finished Good", "Pcs", 80, 100, 12000, 18000, 15),
                    Item("t3", "Laptop Pro 15\"", "Laptop", "Finished Good", "Pcs", 40, 50, 55000, 75000, 10),
                    Item("t4", "Wireless Earbuds", "Audio", "Trading Good", "Pcs", 300, 400, 1500, 2500, 50),
                    Item("t5", "Smart Watch S3", "Wearable", "Trading Good", "Pcs", 100, 120, 4500, 7000, 20),
                    Item("t6", "Bluetooth Speaker", "Audio", "Trading Good", "Pcs", 200, 250, 2000, 3500, 30)
                ),
                parties = listOf("Global Mart", "Horizon Pvt Ltd", "Innovative Tech", "Prime Retail", "Star Enterprises")
            )
        )
    }

    fun getTenantByCode(code: String): Tenant? = tenants.find { it.code.equals(code, ignoreCase = true) }
    val allTenants: List<Tenant> get() = tenants.toList()

    suspend fun login(tenantCode: String, username: String, password: String): String? {
        kotlinx.coroutines.delay(500)
        val expected = users["$tenantCode:$username"] ?: return null
        return if (expected == password) getTenantByCode(tenantCode)?.id else null
    }

    private fun store(tenantId: String): TenantData = stores[tenantId]
        ?: throw IllegalArgumentException("Tenant not found: $tenantId")

    fun getCompanies(tenantId: String) = store(tenantId).companies
    fun getDashboardStats(tenantId: String) = store(tenantId).dashboardStats
    fun getOutstandingSummary(tenantId: String) = store(tenantId).outstandingSummary
    fun getOutstandingLedgers(tenantId: String) = store(tenantId).outstandingLedgers
    fun getOutstandingGroups(tenantId: String) = store(tenantId).outstandingGroups
    fun getItems(tenantId: String) = store(tenantId).items
    fun getParties(tenantId: String) = store(tenantId).parties

    fun getSalesEntries() = listOf(
        SalesEntry("All Entries", "Manage your sales entries and invoices", "Create New Entry"),
        SalesEntry("Check In Report", "View daily check-in reports of your team", "Check-In Now", true),
        SalesEntry("Follow Ups", "Track follow-ups with your customers", "Set Reminder", true),
        SalesEntry("Manage Users", "Add or remove team members", hasArrow = true)
    )

    fun getReports() = listOf(
        ReportItem("Auto Reminders"), ReportItem("Invoice Auto Share"), ReportItem("Top Report"),
        ReportItem("Expenses"), ReportItem("Inactive Customers"), ReportItem("Inactive Items"),
        ReportItem("Ledger Report"), ReportItem("Day Book"), ReportItem("Pending Sales Order"),
        ReportItem("Pending Purchase Order"), ReportItem("Profit & Loss"), ReportItem("Balance Sheet")
    )

    fun getEntryTypes() = listOf("Sales Invoice", "Purchase Invoice", "Receipt", "Payment",
        "Delivery Note", "Sales Order", "Purchase Order", "Quotation", "Receipt Note", "Journal")

    fun getItemCategories(tenantId: String) = store(tenantId).items.map { it.category }.distinct().sorted()
    fun getItemGroups(tenantId: String) = store(tenantId).items.map { it.group }.distinct().sorted()

    fun getLedgers(tenantId: String): List<Ledger> {
        val companies = store(tenantId).companies
        return if (companies.isNotEmpty()) {
            listOf(
                Ledger("l1", "ABC Enterprises", "Cr", "Sundry Debtors", 50000.0, 12345678.0, debit = 200000.0, credit = 12345678.0, date = "2026-06-18", category = "Customer"),
                Ledger("l2", "XYZ Industries", "Dr", "Sundry Creditors", 25000.0, 5678901.0, debit = 5678901.0, credit = 100000.0, date = "2026-06-15", category = "Supplier"),
                Ledger("l3", "PQR Tradelink", "Cr", "Sundry Debtors", 100000.0, 78901234.0, debit = 500000.0, credit = 78901234.0, date = "2026-06-10", category = "Customer"),
                Ledger("l4", "LMN Brothers", "Dr", "Sundry Creditors", 15000.0, 1234567.0, debit = 1234567.0, credit = 50000.0, date = "2026-06-20", category = "Supplier"),
                Ledger("l5", "EFG Corporation", "Cr", "Sundry Debtors", 75000.0, 34567890.0, debit = 300000.0, credit = 34567890.0, date = "2026-05-28", category = "Customer"),
                Ledger("l6", "Amit Traders", "Dr", "Sundry Creditors", 10000.0, 12500.0, debit = 12500.0, credit = 80000.0, date = "2026-06-12", category = "Supplier"),
                Ledger("l7", "Bharat Electronics", "Cr", "Sundry Debtors", 20000.0, 4567890.0, debit = 100000.0, credit = 4567890.0, date = "2026-06-01", category = "Customer"),
                Ledger("l8", "Global Mart", "Cr", "Sundry Debtors", 60000.0, 125000.0, debit = 450000.0, credit = 125000.0, date = "2026-06-05", category = "Customer"),
                Ledger("l9", "Horizon Pvt Ltd", "Dr", "Sundry Creditors", 30000.0, 67890.0, debit = 67890.0, credit = 120000.0, date = "2026-06-22", category = "Supplier"),
                Ledger("l10", "Innovative Tech", "Cr", "Sundry Debtors", 40000.0, 210000.0, debit = 600000.0, credit = 210000.0, date = "2026-04-15", category = "Customer")
            )
        } else emptyList()
    }

    fun getLedgerVouchers(ledgerGuid: String): List<Voucher> = listOf(
        Voucher("v1", "2026-06-18", "Sales", "INV-001", 0.0, 50000.0, 50000.0, "Posted"),
        Voucher("v2", "2026-06-15", "Payment", "PMT-001", 25000.0, 0.0, 25000.0, "Posted"),
        Voucher("v3", "2026-06-10", "Sales", "INV-002", 0.0, 75000.0, 125000.0, "Posted"),
        Voucher("v4", "2026-06-08", "Receipt", "REC-001", 0.0, 30000.0, 155000.0, "Posted"),
        Voucher("v5", "2026-06-05", "Credit Note", "CN-001", 5000.0, 0.0, 150000.0, "Pending"),
        Voucher("v6", "2026-06-01", "Sales", "INV-003", 0.0, 60000.0, 210000.0, "Posted")
    )

    fun getLedgerInvoices(ledgerGuid: String): List<Invoice> = listOf(
        Invoice("i1", "2026-06-18", "INV-001", "Sales", "ABC Enterprises", 50000.0),
        Invoice("i2", "2026-06-10", "INV-002", "Sales", "ABC Enterprises", 75000.0),
        Invoice("i3", "2026-06-01", "INV-003", "Sales", "ABC Enterprises", 60000.0),
        Invoice("i4", "2026-05-25", "INV-004", "Sales", "ABC Enterprises", 45000.0),
        Invoice("i5", "2026-05-15", "INV-005", "Sales", "ABC Enterprises", 55000.0)
    )

    fun getLedgerBills(ledgerGuid: String): List<Bill> = listOf(
        Bill("BILL-001", "ABC Enterprises", "2026-07-18", 50000.0, 25000.0),
        Bill("BILL-002", "ABC Enterprises", "2026-07-10", 75000.0, 75000.0),
        Bill("BILL-003", "ABC Enterprises", "2026-07-01", 60000.0, 10000.0)
    )

    fun getLedgerAgeing(ledgerGuid: String): List<Ageing> = listOf(
        Ageing("0-30", 125000.0),
        Ageing("31-60", 100000.0),
        Ageing("61-90", 45000.0),
        Ageing("90+", 35000.0)
    )

    private val _orders = listOf(
        Order("o1", "SO-001", "Sales", "2026-06-18", "ABC Enterprises", 125000.0, "2026-07-18", "Completed"),
        Order("o2", "PO-001", "Purchase", "2026-06-17", "XYZ Industries", 78000.0, "2026-07-17", "Pending"),
        Order("o3", "SO-002", "Sales", "2026-06-16", "PQR Tradelink", 234000.0, "2026-07-16", "Completed"),
        Order("o4", "PO-002", "Purchase", "2026-06-15", "LMN Brothers", 56000.0, "2026-07-15", "Cancelled"),
        Order("o5", "SO-003", "Sales", "2026-06-14", "EFG Corporation", 345000.0, "2026-08-14", "Pending"),
        Order("o6", "SO-004", "Sales", "2026-06-13", "Amit Traders", 89000.0, "2026-07-13", "Completed"),
        Order("o7", "PO-003", "Purchase", "2026-06-12", "Global Mart", 167000.0, "2026-07-12", "Pending"),
        Order("o8", "SO-005", "Sales", "2026-06-11", "Innovative Tech", 456000.0, "2026-08-11", "Completed"),
        Order("o9", "PO-004", "Purchase", "2026-06-10", "Bharat Electronics", 92000.0, "2026-07-10", "Completed"),
        Order("o10", "SO-006", "Sales", "2026-06-09", "Horizon Pvt Ltd", 198000.0, "2026-07-09", "Cancelled"),
        Order("o11", "PO-005", "Purchase", "2026-06-08", "Delta Supplies", 43000.0, "2026-07-08", "Pending"),
        Order("o12", "SO-007", "Sales", "2026-06-07", "Crystal Distributors", 312000.0, "2026-08-07", "Completed"),
        Order("o13", "SO-008", "Sales", "2026-06-06", "Prime Retail", 67000.0, "2026-07-06", "Pending"),
        Order("o14", "PO-006", "Purchase", "2026-06-05", "Star Enterprises", 284000.0, "2026-07-05", "Completed"),
        Order("o15", "SO-009", "Sales", "2026-06-04", "ABC Enterprises", 155000.0, "2026-07-04", "Pending"),
        Order("o16", "PO-007", "Purchase", "2026-06-03", "XYZ Industries", 72000.0, "2026-07-03", "Completed"),
        Order("o17", "SO-010", "Sales", "2026-06-02", "LMN Brothers", 523000.0, "2026-08-02", "Pending"),
        Order("o18", "PO-008", "Purchase", "2026-06-01", "EFG Corporation", 110000.0, "2026-07-01", "Cancelled"),
        Order("o19", "SO-011", "Sales", "2026-05-30", "PQR Tradelink", 275000.0, "2026-06-30", "Completed"),
        Order("o20", "PO-009", "Purchase", "2026-05-28", "Global Mart", 195000.0, "2026-06-28", "Pending")
    )

    val orderTypes = listOf("All", "Sales", "Purchase")
    val orderStatuses = listOf("All Status", "Pending", "Completed", "Cancelled")

    fun getOrders(): List<Order> = _orders.toList()

    fun getVouchers(): List<Voucher> = listOf(
        Voucher("v1", "2026-06-18", "Sales", "INV-001", 0.0, 50000.0, status = "Approved", party = "ABC Enterprises", narration = "Sales of building materials"),
        Voucher("v2", "2026-06-17", "Payment", "PMT-001", 25000.0, 0.0, status = "Approved", party = "XYZ Suppliers", narration = "Payment for purchase invoice"),
        Voucher("v3", "2026-06-16", "Receipt", "REC-001", 0.0, 30000.0, status = "Approved", party = "PQR Tradelink", narration = "Receipt against outstanding"),
        Voucher("v4", "2026-06-15", "Purchase", "PUR-001", 45000.0, 0.0, status = "Approved", party = "LMN Brothers", narration = "Purchase of raw materials"),
        Voucher("v5", "2026-06-14", "Sales", "INV-002", 0.0, 75000.0, status = "Approved", party = "EFG Corporation", narration = "Sales invoice"),
        Voucher("v6", "2026-06-13", "Journal", "JRN-001", 10000.0, 10000.0, status = "Approved", party = "Contra Entry", narration = "Contra entry adjustment"),
        Voucher("v7", "2026-06-12", "Receipt", "REC-002", 0.0, 15000.0, status = "Cancelled", party = "Amit Traders", narration = "Cancelled receipt"),
        Voucher("v8", "2026-06-11", "Sales", "INV-003", 0.0, 60000.0, status = "Approved", party = "Global Mart", narration = "Sales of electronics"),
        Voucher("v9", "2026-06-10", "Payment", "PMT-002", 12000.0, 0.0, status = "Approved", party = "Electricity Board", narration = "Monthly electricity bill"),
        Voucher("v10", "2026-06-09", "Purchase", "PUR-002", 35000.0, 0.0, status = "Cancelled", party = "Horizon Pvt Ltd", narration = "Cancelled purchase"),
        Voucher("v11", "2026-06-08", "Sales", "INV-004", 0.0, 85000.0, status = "Approved", party = "Innovative Tech", narration = "Sales of IT equipment"),
        Voucher("v12", "2026-06-07", "Journal", "JRN-002", 5000.0, 5000.0, status = "Approved", party = "Opening Balance", narration = "Opening balance entry"),
        Voucher("v13", "2026-06-06", "Receipt", "REC-003", 0.0, 22000.0, status = "Approved", party = "Bharat Electronics", narration = "Receipt against invoice"),
        Voucher("v14", "2026-06-05", "Payment", "PMT-003", 8000.0, 0.0, status = "Approved", party = "Rent Payable", narration = "Office rent payment"),
        Voucher("v15", "2026-05-30", "Sales", "INV-005", 0.0, 95000.0, status = "Approved", party = "ABC Enterprises", narration = "Bulk sales order")
    )

    val voucherTypes = listOf("Sales", "Purchase", "Payment", "Receipt", "Journal")
    val voucherStatuses = listOf("All Status", "Approved", "Cancelled")

    fun createVoucher(voucher: Voucher): Voucher {
        val newId = "v${System.currentTimeMillis()}"
        return voucher.copy(id = newId)
    }

    fun updateVoucher(id: String, voucher: Voucher): Voucher? = voucher.copy(id = id)

    fun deleteVoucher(id: String): Boolean = true

    val units = listOf("Nos", "Kg", "Pcs", "Bags", "Box", "Roll", "Ton", "Set", "Litre")

    data class FollowUp(val id: String, val party: String, val purpose: String, val date: String, val status: String, val notes: String = "")
    data class CheckIn(val id: String, val user: String, val time: String, val location: String, val status: String)
    data class AppUser(val id: String, val name: String, val email: String, val role: String, val active: Boolean = true)
    data class Reminder(val id: String, val title: String, val description: String, val time: String, val enabled: Boolean = true)
    data class Transaction(val id: String, val type: String, val party: String, val amount: String, val date: String, val status: String)

    fun getFollowUps(tenantId: String) = listOf(
        FollowUp("f1", "ABC Enterprises", "Payment Follow-up", "2026-06-22", "Pending", "Call after 2 PM"),
        FollowUp("f2", "XYZ Industries", "Quote Submission", "2026-06-21", "Completed", "Submitted via email"),
        FollowUp("f3", "PQR Tradelink", "Contract Renewal", "2026-06-25", "Pending", "Draft sent for review"),
        FollowUp("f4", "LMN Brothers", "Product Demo", "2026-06-20", "In Progress", "Schedule for Thursday"),
        FollowUp("f5", "EFG Corporation", "Invoice Dispute", "2026-06-28", "Pending", "Awaiting documents")
    )

    fun getCheckIns(tenantId: String) = listOf(
        CheckIn("c1", "Rahul Sharma", "09:15 AM", "Site A - Downtown", "Checked In"),
        CheckIn("c2", "Priya Patel", "08:45 AM", "Office HQ", "Checked In"),
        CheckIn("c3", "Amit Singh", "09:30 AM", "Site B - Industrial Area", "Checked In"),
        CheckIn("c4", "Sneha Reddy", "10:00 AM", "Client Visit - ABC Corp", "Late"),
        CheckIn("c5", "Vikram Joshi", "08:30 AM", "Office HQ", "Checked In"),
        CheckIn("c6", "Neha Gupta", "09:45 AM", "Site C - Mall Road", "Checked In"),
        CheckIn("c7", "Rohit Verma", "10:15 AM", "Remote - Home Office", "Late")
    )

    fun getUsers(tenantId: String) = listOf(
        AppUser("u1", "Rahul Sharma", "rahul@tally.com", "Sales Manager", true),
        AppUser("u2", "Priya Patel", "priya@tally.com", "Sales Rep", true),
        AppUser("u3", "Amit Singh", "amit@tally.com", "Sales Rep", true),
        AppUser("u4", "Sneha Reddy", "sneha@tally.com", "Accountant", true),
        AppUser("u5", "Vikram Joshi", "vikram@tally.com", "Sales Rep", false),
        AppUser("u6", "Neha Gupta", "neha@tally.com", "Admin", true),
        AppUser("u7", "Rohit Verma", "rohit@tally.com", "Sales Rep", true)
    )

    fun getReminders(tenantId: String) = listOf(
        Reminder("r1", "Overdue Reminder", "Sent daily at 10:00 AM for overdue invoices", "10:00 AM", true),
        Reminder("r2", "Upcoming Payment Alert", "Sent 3 days before due date", "09:00 AM", true),
        Reminder("r3", "Follow-up Reminder", "Remind sales team of pending follow-ups", "11:00 AM", false),
        Reminder("r4", "Stock Reorder Alert", "Notify when stock is below reorder level", "08:00 AM", true),
        Reminder("r5", "Daily Summary Report", "End-of-day summary to all managers", "06:00 PM", false)
    )

    fun getTransactions(tenantId: String) = listOf(
        Transaction("tr1", "Sales Invoice", "ABC Enterprises", "₹ 1,23,456", "2026-06-18", "Posted"),
        Transaction("tr2", "Purchase Invoice", "XYZ Suppliers", "₹ 78,900", "2026-06-17", "Pending"),
        Transaction("tr3", "Receipt", "PQR Tradelink", "₹ 50,000", "2026-06-16", "Posted"),
        Transaction("tr4", "Payment", "Electricity Board", "₹ 12,340", "2026-06-15", "Posted"),
        Transaction("tr5", "Sales Order", "LMN Brothers", "₹ 2,34,567", "2026-06-14", "Confirmed"),
        Transaction("tr6", "Delivery Note", "EFG Corporation", "₹ 89,012", "2026-06-13", "Pending")
    )
}
