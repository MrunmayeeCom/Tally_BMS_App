import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:bmstally_app/providers/app_provider.dart';
import 'package:bmstally_app/models/dashboard_stat.dart';
import 'package:bmstally_app/widgets/info_banner.dart';
import 'package:bmstally_app/screens/items/items_screen.dart';

class DashboardScreen extends StatelessWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<AppProvider>();
    final stats = provider.dashboardStats;

    return Column(
      children: [
        _appBar(context, provider),
        _syncBar(provider),
        Expanded(
          child: ListView(
            padding: const EdgeInsets.symmetric(vertical: 8),
            children: [
              _quickActions(context),
              InfoBanner(
                icon: Icons.campaign,
                title: 'Email verification pending',
                subtitle: "Please open the mail from us and tap on 'Verify Email'.",
              ),
              _dateSelector(),
              const Divider(height: 1),
              ...stats.map((s) => _statCard(s)),
            ],
          ),
        ),
      ],
    );
  }

  Widget _appBar(BuildContext context, AppProvider provider) {
    return Container(
      color: Colors.blue[700],
      padding: EdgeInsets.only(top: MediaQuery.of(context).padding.top),
      child: Row(
        children: [
          IconButton(
            icon: const Icon(Icons.menu, color: Colors.white),
            onPressed: () => Scaffold.of(context).openDrawer(),
          ),
          Expanded(
            child: DropdownButtonHideUnderline(
              child: DropdownButton<String>(
                value: provider.selectedCompany.name,
                isDense: true,
                dropdownColor: Colors.blue[700],
                style: const TextStyle(color: Colors.white, fontSize: 16),
                icon: const Icon(Icons.arrow_drop_down, color: Colors.white),
                items: provider.companies
                    .map((c) => DropdownMenuItem(value: c.name, child: Text(c.name)))
                    .toList(),
                onChanged: (val) {
                  final company = provider.companies.firstWhere((c) => c.name == val);
                  provider.selectCompany(company);
                },
              ),
            ),
          ),
          IconButton(
            icon: const Icon(Icons.help_outline, color: Colors.white),
            onPressed: () => Navigator.of(context).pushNamed('/help'),
          ),
        ],
      ),
    );
  }

  Widget _syncBar(AppProvider provider) {
    final tenant = provider.currentTenant;
    return Container(
      color: Colors.blue[50],
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
      child: Row(
        children: [
          const Icon(Icons.business, size: 14, color: Colors.grey),
          const SizedBox(width: 4),
          Text(tenant?.name ?? '',
               style: TextStyle(fontSize: 11, color: Colors.grey[700])),
          const SizedBox(width: 10),
          Icon(Icons.sync, size: 12, color: Colors.grey[500]),
          const SizedBox(width: 4),
          Text('18 Jun 26',
               style: TextStyle(fontSize: 10, color: Colors.grey[500])),
          const Spacer(),
          Icon(Icons.error_outline, size: 14, color: Colors.red[400]),
        ],
      ),
    );
  }

  Widget _quickActions(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Row(
        children: [
          Expanded(
            child: Card(
              elevation: 1,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              child: InkWell(
                borderRadius: BorderRadius.circular(12),
                onTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => const ItemsScreen(),
                    ),
                  );
                },
                child: const Padding(
                  padding: EdgeInsets.symmetric(vertical: 24, horizontal: 16),
                  child: Column(
                    children: [
                      Icon(Icons.inventory_2, size: 36, color: Colors.indigo),
                      SizedBox(height: 8),
                      Text('Items', style: TextStyle(fontSize: 14)),
                    ],
                  ),
                ),
              ),
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Card(
              elevation: 1,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              child: InkWell(
                borderRadius: BorderRadius.circular(12),
                onTap: () {
                  Navigator.of(context).pushNamed('/wallet');
                },
                child: const Padding(
                  padding: EdgeInsets.symmetric(vertical: 24, horizontal: 16),
                  child: Column(
                    children: [
                      Icon(Icons.people, size: 36, color: Colors.indigo),
                      SizedBox(height: 8),
                      Text('Party', style: TextStyle(fontSize: 14)),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _dateSelector() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.arrow_back_ios, size: 14),
          const SizedBox(width: 8),
          const Icon(Icons.calendar_today, size: 16),
          const SizedBox(width: 8),
          const Text('01 Apr 26 - 31 Mar 27',
              style: TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
          const SizedBox(width: 8),
          const Icon(Icons.arrow_forward_ios, size: 14),
        ],
      ),
    );
  }

  Widget _statCard(DashboardStat stat) {
    return Card(
      elevation: 0,
      color: Colors.grey[50],
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
      margin: const EdgeInsets.symmetric(vertical: 4, horizontal: 16),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: Colors.indigo.withValues(alpha: 0.1),
          child: Icon(_iconFor(stat.label), color: Colors.indigo),
        ),
        title: Text(stat.amount,
            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
        subtitle: Text(stat.label, style: const TextStyle(fontSize: 12)),
      ),
    );
  }

  IconData _iconFor(String label) {
    if (label.contains('Credit Note')) return Icons.assignment_return;
    if (label.contains('Debit Note')) return Icons.assignment_late;
    if (label == 'Receipt') return Icons.account_balance_wallet;
    if (label == 'Payment') return Icons.money_off;
    if (label.contains('Receivable')) return Icons.receipt_long;
    if (label.contains('Payable')) return Icons.payments;
    if (label.contains('Cash') || label.contains('Bank')) return Icons.account_balance;
    if (label.contains('Sales Order')) return Icons.shopping_bag;
    if (label.contains('Purchase Order')) return Icons.shopping_cart;
    if (label.contains('Delivery Note')) return Icons.local_shipping;
    if (label.contains('Receipt Note')) return Icons.note_add;
    return Icons.circle;
  }
}
