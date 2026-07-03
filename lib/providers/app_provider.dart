import 'package:flutter/material.dart';
import 'package:bmstally_app/models/company.dart';
import 'package:bmstally_app/models/tenant.dart';
import 'package:bmstally_app/models/dashboard_stat.dart';
import 'package:bmstally_app/models/outstanding_item.dart';
import 'package:bmstally_app/models/sales_entry.dart';
import 'package:bmstally_app/models/report_item.dart';
import 'package:bmstally_app/services/mock_data_service.dart';

class AppProvider extends ChangeNotifier {
  final MockDataService _dataService = MockDataService();

  int _tabIndex = 0;
  Tenant? _currentTenant;
  Company _selectedCompany = const Company(id: '1', name: 'Tally Solutions');
  List<Company> _companies = [];
  String _userName = '';
  String _userEmail = '';
  bool _isLoggedIn = false;

  int get tabIndex => _tabIndex;
  Tenant? get currentTenant => _currentTenant;
  Company get selectedCompany => _selectedCompany;
  List<Company> get companies => _companies;
  String get userName => _userName;
  String get userEmail => _userEmail;
  bool get isLoggedIn => _isLoggedIn;
  String get tenantId => _currentTenant?.id ?? '';

  void setTab(int index) {
    _tabIndex = index;
    notifyListeners();
  }

  void selectCompany(Company company) {
    _selectedCompany = company;
    notifyListeners();
  }

  List<DashboardStat> get dashboardStats =>
      _dataService.getDashboardStats(tenantId);

  String get outstandingSummary =>
      _dataService.getOutstandingSummary(tenantId);

  List<OutstandingItem> get outstandingLedgers =>
      _dataService.getOutstandingLedgers(tenantId);

  List<List<String>> get outstandingGroups =>
      _dataService.getOutstandingGroups(tenantId);

  List<SalesEntry> get salesEntries =>
      _dataService.getSalesEntries();

  List<ReportItem> get reports => _dataService.getReports();

  List<String> get entryTypes => _dataService.getEntryTypes();
  List<String> get parties => _dataService.getParties(tenantId);

  Future<bool> login(String tenantCode, String username, String password) async {
    final tenantId = await _dataService.login(tenantCode, username, password);
    if (tenantId != null) {
      final tenant = _dataService.getTenantByCode(tenantCode);
      _isLoggedIn = true;
      _currentTenant = tenant;
      _userName = username;
      _userEmail = '$username@${tenantCode.toLowerCase()}.com';
      _companies = _dataService.getCompanies(tenantId);
      _selectedCompany = _companies.first;
      notifyListeners();
      return true;
    }
    return false;
  }

  void logout() {
    _isLoggedIn = false;
    _currentTenant = null;
    _userName = '';
    _userEmail = '';
    _companies = [];
    _tabIndex = 0;
    notifyListeners();
  }
}
