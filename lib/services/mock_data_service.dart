import 'package:bmstally_app/models/tenant.dart';
import 'package:bmstally_app/models/company.dart';
import 'package:bmstally_app/models/dashboard_stat.dart';
import 'package:bmstally_app/models/outstanding_item.dart';
import 'package:bmstally_app/models/sales_entry.dart';
import 'package:bmstally_app/models/report_item.dart';
import 'package:bmstally_app/models/item.dart';

class _TenantData {
  final List<Company> companies;
  final List<DashboardStat> dashboardStats;
  final String outstandingSummary;
  final List<OutstandingItem> outstandingLedgers;
  final List<List<String>> outstandingGroups;
  final List<Item> items;
  final List<String> parties;

  const _TenantData({
    required this.companies,
    required this.dashboardStats,
    required this.outstandingSummary,
    required this.outstandingLedgers,
    required this.outstandingGroups,
    required this.items,
    required this.parties,
  });
}

class MockDataService {
  static final MockDataService _instance = MockDataService._();
  factory MockDataService() => _instance;
  MockDataService._();

  final Map<String, _TenantData> _stores = {};
  final Map<String, String> _users = {}; // "tenantCode:username" -> "password"
  final List<Tenant> _tenants = [];

  void _registerTenant({
    required String id,
    required String code,
    required String name,
    String? gstin,
    required String adminUser,
    required String adminPass,
    required _TenantData data,
  }) {
    _tenants.add(Tenant(id: id, code: code, name: name, gstin: gstin));
    _stores[id] = data;
    _users['$code:$adminUser'] = adminPass;
  }

  void seed() {
    if (_tenants.isNotEmpty) return;

    _registerTenant(
      id: 't1',
      code: 'tally',
      name: 'Tally Solutions',
      gstin: '27AAACT1234A1Z5',
      adminUser: 'admin',
      adminPass: 'admin123',
      data: const _TenantData(
        companies: [
          Company(id: '1', name: 'Tally Solutions', gstin: '27AAACT1234A1Z5'),
          Company(id: '2', name: 'BMS Corp', gstin: '27AABCS5678B1Z6'),
        ],
        dashboardStats: [
          DashboardStat(label: 'Sales - Credit Note (Gross)', amount: '₹ 12,37,41,37,96,173'),
          DashboardStat(label: 'Purchase - Debit Note (Gross)', amount: '₹ 10,61,34,78,07,790'),
          DashboardStat(label: 'Receipt', amount: '₹ 5,67,89,012'),
          DashboardStat(label: 'Payment', amount: '₹ 4,56,78,901'),
          DashboardStat(label: 'Outstanding Receivable', amount: '₹ 12,37,41,37,96,173'),
          DashboardStat(label: 'Outstanding Payable', amount: '₹ 10,61,34,78,07,790'),
          DashboardStat(label: 'Cash / Bank Balance', amount: '₹ 5,67,89,012'),
          DashboardStat(label: 'Sales Order', amount: '₹ 4,56,78,901'),
          DashboardStat(label: 'Purchase Order', amount: '₹ 3,45,67,890'),
          DashboardStat(label: 'Delivery Note', amount: '₹ 2,34,56,789'),
          DashboardStat(label: 'Receipt Note', amount: '₹ 1,23,45,678'),
        ],
        outstandingSummary: '₹ 12,37,41,37,96,173',
        outstandingLedgers: [
          OutstandingItem(partyName: 'ABC Enterprises', creditInfo: 'Cr', amount: '₹ 1,23,45,678', isCredit: true, meta: '30 days', paymentInfo: 'Avg 28 days'),
          OutstandingItem(partyName: 'XYZ Industries', creditInfo: 'Dr', amount: '₹ 56,78,901', isCredit: false, meta: '15 days', paymentInfo: 'Avg 14 days'),
          OutstandingItem(partyName: 'PQR Tradelink', creditInfo: 'Cr', amount: '₹ 7,89,01,234', isCredit: true, meta: '60 days', paymentInfo: 'Avg 55 days'),
          OutstandingItem(partyName: 'LMN Brothers', creditInfo: 'Dr', amount: '₹ 12,34,567', isCredit: false, meta: '45 days', paymentInfo: 'Avg 40 days'),
          OutstandingItem(partyName: 'EFG Corporation', creditInfo: 'Cr', amount: '₹ 3,45,67,890', isCredit: true, meta: '90 days', paymentInfo: 'Avg 85 days'),
        ],
        outstandingGroups: [
          ['Sundry Debtors', '₹ 12,37,41,37,96,173'],
          ['Sundry Creditors', '₹ 10,61,34,78,07,790'],
          ['Loans & Advances', '₹ 45,67,890'],
        ],
        items: [
          Item(id: '1', name: 'Cement 53 Grade', category: 'Building Material', group: 'Raw Material', unit: 'Bags', quantity: 250, openingStock: 300, purchaseRate: 350, salesRate: 420, reorderLevel: 50),
          Item(id: '2', name: 'Steel Rods 12mm', category: 'Building Material', group: 'Raw Material', unit: 'Kg', quantity: 5000, openingStock: 6000, purchaseRate: 72, salesRate: 85, reorderLevel: 1000),
          Item(id: '3', name: 'Bricks (Red)', category: 'Building Material', group: 'Raw Material', unit: 'Nos', quantity: 15000, openingStock: 20000, purchaseRate: 8, salesRate: 12, reorderLevel: 5000),
          Item(id: '4', name: 'Paint - White 20L', category: 'Paint', group: 'Finished Good', unit: 'Pcs', quantity: 45, openingStock: 60, purchaseRate: 1800, salesRate: 2400, reorderLevel: 10),
          Item(id: '5', name: 'Paint - Blue 10L', category: 'Paint', group: 'Finished Good', unit: 'Pcs', quantity: 30, openingStock: 40, purchaseRate: 1200, salesRate: 1600, reorderLevel: 8),
          Item(id: '6', name: 'PVC Pipe 4 inch', category: 'Plumbing', group: 'Trading Good', unit: 'Pcs', quantity: 120, openingStock: 150, purchaseRate: 450, salesRate: 580, reorderLevel: 20),
          Item(id: '7', name: 'PVC Pipe 2 inch', category: 'Plumbing', group: 'Trading Good', unit: 'Pcs', quantity: 200, openingStock: 250, purchaseRate: 280, salesRate: 370, reorderLevel: 30),
          Item(id: '8', name: 'Tiles - Floor 2x2', category: 'Tiles', group: 'Finished Good', unit: 'Box', quantity: 80, openingStock: 100, purchaseRate: 650, salesRate: 890, reorderLevel: 15),
          Item(id: '9', name: 'Tiles - Wall 1x1', category: 'Tiles', group: 'Finished Good', unit: 'Box', quantity: 60, openingStock: 80, purchaseRate: 520, salesRate: 720, reorderLevel: 10),
          Item(id: '10', name: 'Sand (Fine)', category: 'Building Material', group: 'Raw Material', unit: 'Ton', quantity: 0, openingStock: 20, purchaseRate: 1200, salesRate: 1600, reorderLevel: 5),
          Item(id: '11', name: 'Electrical Wire 1.5mm', category: 'Electrical', group: 'Trading Good', unit: 'Roll', quantity: 90, openingStock: 120, purchaseRate: 950, salesRate: 1250, reorderLevel: 15),
          Item(id: '12', name: 'Switch Board 6 Module', category: 'Electrical', group: 'Trading Good', unit: 'Pcs', quantity: 150, openingStock: 200, purchaseRate: 180, salesRate: 250, reorderLevel: 25),
          Item(id: '13', name: 'Water Tank 1000L', category: 'Plumbing', group: 'Trading Good', unit: 'Pcs', quantity: 12, openingStock: 15, purchaseRate: 4500, salesRate: 6200, reorderLevel: 3),
          Item(id: '14', name: 'Sanitaryware - WC', category: 'Sanitary', group: 'Finished Good', unit: 'Pcs', quantity: 25, openingStock: 30, purchaseRate: 3200, salesRate: 4500, reorderLevel: 5),
          Item(id: '15', name: 'Magnetic Tiles (Set)', category: 'Tiles', group: 'Finished Good', unit: 'Set', quantity: 0, openingStock: 10, purchaseRate: 2800, salesRate: 3800, reorderLevel: 2),
          Item(id: '16', name: 'Adhesive - Tile Fix', category: 'Building Material', group: 'Consumable', unit: 'Kg', quantity: 300, openingStock: 400, purchaseRate: 45, salesRate: 65, reorderLevel: 50),
          Item(id: '17', name: 'LED Panel Light 2x2', category: 'Electrical', group: 'Trading Good', unit: 'Pcs', quantity: 40, openingStock: 50, purchaseRate: 850, salesRate: 1200, reorderLevel: 8),
          Item(id: '18', name: 'CPVC Pipe 1 inch', category: 'Plumbing', group: 'Trading Good', unit: 'Pcs', quantity: 0, openingStock: 100, purchaseRate: 180, salesRate: 240, reorderLevel: 20),
        ],
        parties: ['ABC Enterprises', 'XYZ Industries', 'PQR Tradelink', 'LMN Brothers', 'EFG Corporation', 'Amit Traders', 'Bharat Electronics'],
      ),
    );

    _registerTenant(
      id: 't2',
      code: 'bmscorp',
      name: 'BMS Corp',
      gstin: '27AABCS5678B1Z6',
      adminUser: 'admin',
      adminPass: 'admin456',
      data: const _TenantData(
        companies: [
          Company(id: '1', name: 'BMS Corp', gstin: '27AABCS5678B1Z6'),
          Company(id: '2', name: 'BMS Retail', gstin: '27AABCR9012D1Z8'),
        ],
        dashboardStats: [
          DashboardStat(label: 'Sales - Credit Note (Gross)', amount: '₹ 1,200'),
          DashboardStat(label: 'Purchase - Debit Note (Gross)', amount: '₹ 500'),
          DashboardStat(label: 'Receipt', amount: '₹ 5,100'),
          DashboardStat(label: 'Payment', amount: '₹ 4,200'),
          DashboardStat(label: 'Outstanding Receivable', amount: '₹ 45,67,890'),
          DashboardStat(label: 'Outstanding Payable', amount: '₹ 32,10,987'),
          DashboardStat(label: 'Cash / Bank Balance', amount: '₹ 12,50,000'),
          DashboardStat(label: 'Sales Order', amount: '₹ 8,75,000'),
          DashboardStat(label: 'Purchase Order', amount: '₹ 5,20,000'),
          DashboardStat(label: 'Delivery Note', amount: '₹ 3,10,000'),
          DashboardStat(label: 'Receipt Note', amount: '₹ 2,45,000'),
        ],
        outstandingSummary: '₹ 45,67,890',
        outstandingLedgers: [
          OutstandingItem(partyName: 'Amit Traders', creditInfo: 'Cr', amount: '₹ 12,500', isCredit: true, meta: '30 days', paymentInfo: 'Avg 25 days'),
          OutstandingItem(partyName: 'Bharat Electronics', creditInfo: 'Dr', amount: '₹ 8,200', isCredit: false, meta: '15 days', paymentInfo: 'Avg 12 days'),
          OutstandingItem(partyName: 'Crystal Distributors', creditInfo: 'Cr', amount: '₹ 25,000', isCredit: true, meta: '45 days', paymentInfo: 'Avg 40 days'),
          OutstandingItem(partyName: 'Delta Supplies', creditInfo: 'Dr', amount: '₹ 5,100', isCredit: false, meta: '10 days', paymentInfo: 'Avg 8 days'),
        ],
        outstandingGroups: [
          ['Sundry Debtors', '₹ 45,67,890'],
          ['Sundry Creditors', '₹ 32,10,987'],
          ['Loans & Advances', '₹ 12,50,000'],
        ],
        items: [
          Item(id: 'b1', name: 'Office Chair', category: 'Furniture', group: 'Finished Good', unit: 'Pcs', quantity: 45, openingStock: 50, purchaseRate: 4500, salesRate: 6500, reorderLevel: 10),
          Item(id: 'b2', name: 'Standing Desk', category: 'Furniture', group: 'Finished Good', unit: 'Pcs', quantity: 20, openingStock: 25, purchaseRate: 12000, salesRate: 18000, reorderLevel: 5),
          Item(id: 'b3', name: 'LED Monitor 24"', category: 'Electronics', group: 'Trading Good', unit: 'Pcs', quantity: 60, openingStock: 80, purchaseRate: 8500, salesRate: 12000, reorderLevel: 15),
          Item(id: 'b4', name: 'Wireless Keyboard', category: 'Electronics', group: 'Trading Good', unit: 'Pcs', quantity: 120, openingStock: 150, purchaseRate: 1200, salesRate: 1800, reorderLevel: 20),
          Item(id: 'b5', name: 'USB-C Hub', category: 'Electronics', group: 'Trading Good', unit: 'Pcs', quantity: 200, openingStock: 250, purchaseRate: 800, salesRate: 1400, reorderLevel: 30),
        ],
        parties: ['Amit Traders', 'Bharat Electronics', 'Crystal Distributors', 'Delta Supplies'],
      ),
    );

    _registerTenant(
      id: 't3',
      code: 'techmart',
      name: 'TechMart India',
      gstin: '29AAACT9012C1Z7',
      adminUser: 'admin',
      adminPass: 'admin789',
      data: const _TenantData(
        companies: [
          Company(id: '1', name: 'TechMart India', gstin: '29AAACT9012C1Z7'),
          Company(id: '2', name: 'TechMart Wholesale', gstin: '29AABCT3456E1Z0'),
        ],
        dashboardStats: [
          DashboardStat(label: 'Sales - Credit Note (Gross)', amount: '₹ 12,340'),
          DashboardStat(label: 'Purchase - Debit Note (Gross)', amount: '₹ 8,760'),
          DashboardStat(label: 'Receipt', amount: '₹ 98,765'),
          DashboardStat(label: 'Payment', amount: '₹ 76,543'),
          DashboardStat(label: 'Outstanding Receivable', amount: '₹ 2,34,56,789'),
          DashboardStat(label: 'Outstanding Payable', amount: '₹ 1,98,76,543'),
          DashboardStat(label: 'Cash / Bank Balance', amount: '₹ 56,78,900'),
          DashboardStat(label: 'Sales Order', amount: '₹ 34,56,789'),
          DashboardStat(label: 'Purchase Order', amount: '₹ 23,45,678'),
          DashboardStat(label: 'Delivery Note', amount: '₹ 12,34,567'),
          DashboardStat(label: 'Receipt Note', amount: '₹ 8,90,123'),
        ],
        outstandingSummary: '₹ 2,34,56,789',
        outstandingLedgers: [
          OutstandingItem(partyName: 'Global Mart', creditInfo: 'Cr', amount: '₹ 1,25,000', isCredit: true, meta: '60 days', paymentInfo: 'Avg 55 days'),
          OutstandingItem(partyName: 'Horizon Pvt Ltd', creditInfo: 'Dr', amount: '₹ 67,890', isCredit: false, meta: '20 days', paymentInfo: 'Avg 18 days'),
          OutstandingItem(partyName: 'Innovative Tech', creditInfo: 'Cr', amount: '₹ 2,10,000', isCredit: true, meta: '90 days', paymentInfo: 'Avg 85 days'),
        ],
        outstandingGroups: [
          ['Sundry Debtors', '₹ 2,34,56,789'],
          ['Sundry Creditors', '₹ 1,98,76,543'],
          ['Loans & Advances', '₹ 56,78,900'],
        ],
        items: [
          Item(id: 't1', name: 'Smartphone X Pro', category: 'Mobile', group: 'Finished Good', unit: 'Pcs', quantity: 150, openingStock: 200, purchaseRate: 18000, salesRate: 25000, reorderLevel: 25),
          Item(id: 't2', name: 'Tablet Z10', category: 'Mobile', group: 'Finished Good', unit: 'Pcs', quantity: 80, openingStock: 100, purchaseRate: 12000, salesRate: 18000, reorderLevel: 15),
          Item(id: 't3', name: 'Laptop Pro 15"', category: 'Laptop', group: 'Finished Good', unit: 'Pcs', quantity: 40, openingStock: 50, purchaseRate: 55000, salesRate: 75000, reorderLevel: 10),
          Item(id: 't4', name: 'Wireless Earbuds', category: 'Audio', group: 'Trading Good', unit: 'Pcs', quantity: 300, openingStock: 400, purchaseRate: 1500, salesRate: 2500, reorderLevel: 50),
          Item(id: 't5', name: 'Smart Watch S3', category: 'Wearable', group: 'Trading Good', unit: 'Pcs', quantity: 100, openingStock: 120, purchaseRate: 4500, salesRate: 7000, reorderLevel: 20),
          Item(id: 't6', name: 'Bluetooth Speaker', category: 'Audio', group: 'Trading Good', unit: 'Pcs', quantity: 200, openingStock: 250, purchaseRate: 2000, salesRate: 3500, reorderLevel: 30),
        ],
        parties: ['Global Mart', 'Horizon Pvt Ltd', 'Innovative Tech', 'Prime Retail', 'Star Enterprises'],
      ),
    );
  }

  Tenant? getTenantByCode(String code) {
    return _tenants.where((t) => t.code.toLowerCase() == code.toLowerCase()).firstOrNull;
  }

  List<Tenant> get tenants => List.unmodifiable(_tenants);

  Future<String?> login(String tenantCode, String username, String password) async {
    await Future.delayed(const Duration(milliseconds: 500));
    final expected = _users['$tenantCode:$username'];
    if (expected == null || expected != password) return null;
    final tenant = getTenantByCode(tenantCode);
    return tenant?.id;
  }

  _TenantData _store(String tenantId) {
    final s = _stores[tenantId];
    if (s == null) throw Exception('Tenant not found: $tenantId');
    return s;
  }

  List<Company> getCompanies(String tenantId) => _store(tenantId).companies;
  List<DashboardStat> getDashboardStats(String tenantId) => _store(tenantId).dashboardStats;
  String getOutstandingSummary(String tenantId) => _store(tenantId).outstandingSummary;
  List<OutstandingItem> getOutstandingLedgers(String tenantId) => _store(tenantId).outstandingLedgers;
  List<List<String>> getOutstandingGroups(String tenantId) => _store(tenantId).outstandingGroups;
  List<Item> getItems(String tenantId) => _store(tenantId).items;
  List<String> getParties(String tenantId) => _store(tenantId).parties;

  List<SalesEntry> getSalesEntries() => const [
    SalesEntry(title: 'All Entries', description: 'Manage your sales entries and invoices', actionLabel: 'Create New Entry'),
    SalesEntry(title: 'Check In Report', description: 'View daily check-in reports of your team', actionLabel: 'Check-In Now', hasArrow: true),
    SalesEntry(title: 'Follow Ups', description: 'Track follow-ups with your customers', actionLabel: 'Set Reminder', hasArrow: true),
    SalesEntry(title: 'Manage Users', description: 'Add or remove team members', hasArrow: true),
  ];

  List<ReportItem> getReports() => const [
    ReportItem(title: 'Auto Reminders'),
    ReportItem(title: 'Invoice Auto Share'),
    ReportItem(title: 'Top Report'),
    ReportItem(title: 'Expenses'),
    ReportItem(title: 'Inactive Customers'),
    ReportItem(title: 'Inactive Items'),
    ReportItem(title: 'Ledger Report'),
    ReportItem(title: 'Day Book'),
    ReportItem(title: 'Pending Sales Order'),
    ReportItem(title: 'Pending Purchase Order'),
    ReportItem(title: 'Profit & Loss'),
    ReportItem(title: 'Balance Sheet'),
  ];

  List<String> getEntryTypes() => [
    'Sales Invoice', 'Purchase Invoice', 'Receipt', 'Payment',
    'Delivery Note', 'Sales Order', 'Purchase Order', 'Quotation',
    'Receipt Note', 'Journal',
  ];

  List<String> getItemCategories(String tenantId) {
    final items = _store(tenantId).items;
    return items.map((i) => i.category).toSet().toList()..sort();
  }

  List<String> getItemGroups(String tenantId) {
    final items = _store(tenantId).items;
    return items.map((i) => i.group).toSet().toList()..sort();
  }

  List<String> getUnits() => ['Nos', 'Kg', 'Pcs', 'Bags', 'Box', 'Roll', 'Ton', 'Set', 'Litre'];
}
