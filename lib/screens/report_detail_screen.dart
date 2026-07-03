import 'package:flutter/material.dart';

class ReportDetailScreen extends StatelessWidget {
  final String title;

  const ReportDetailScreen({super.key, required this.title});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.blue[700],
        foregroundColor: Colors.white,
        title: Text(title),
        actions: [
          IconButton(
            icon: const Icon(Icons.share),
            onPressed: () => _showShare(context),
          ),
          IconButton(
            icon: const Icon(Icons.more_vert),
            onPressed: () => _showOptions(context),
          ),
        ],
      ),
      body: _buildBody(context),
    );
  }

  Widget _buildBody(BuildContext context) {
    final i = title.toLowerCase();

    if (i.contains('auto reminder') || i.contains('invoice auto share')) {
      return _simpleList(context, _toggleItems());
    }
    if (i.contains('top')) return _topList();
    if (i.contains('expense')) return _expenseList();
    if (i.contains('inactive customer')) return _inactiveList(context, 'customers');
    if (i.contains('inactive item')) return _inactiveList(context, 'items');
    if (i.contains('ledger')) return _ledgerReport();
    if (i.contains('day book')) return _dayBook();
    if (i.contains('pending sales')) return _pendingList('Sales Order');
    if (i.contains('pending purchase')) return _pendingList('Purchase Order');
    if (i.contains('profit')) return _pnlView();
    if (i.contains('balance sheet')) return _balanceSheet();

    return Center(child: Text(title, style: const TextStyle(fontSize: 18)));
  }

  List<Map<String, String>> _toggleItems() {
    return [
      {'title': 'Overdue Reminder', 'status': 'Active', 'subtitle': 'Sent daily at 10:00 AM'},
      {'title': 'Upcoming Payment Alert', 'status': 'Active', 'subtitle': 'Sent 3 days before due date'},
      {'title': 'Invoice Share', 'status': 'Paused', 'subtitle': 'Auto-share via email'},
    ];
  }

  Widget _simpleList(BuildContext context, List<Map<String, String>> items) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        ...items.map((item) => Card(
          margin: const EdgeInsets.symmetric(vertical: 4),
          elevation: 0,
          color: Colors.grey[50],
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          child: SwitchListTile(
            secondary: Icon(Icons.notifications, color: Colors.blue[700]),
            title: Text(item['title']!, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w500)),
            subtitle: Text(item['subtitle']!, style: TextStyle(fontSize: 11, color: Colors.grey[700])),
            value: item['status'] == 'Active',
            onChanged: (_) => ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text('${item['title']} toggled')),
            ),
          ),
        )),
      ],
    );
  }

  Widget _topList() {
    final items = [
      {'rank': '1', 'name': 'ABC Enterprises', 'value': '₹ 1,23,45,678'},
      {'rank': '2', 'name': 'PQR Tradelink', 'value': '₹ 89,01,234'},
      {'rank': '3', 'name': 'EFG Corporation', 'value': '₹ 45,67,890'},
      {'rank': '4', 'name': 'XYZ Industries', 'value': '₹ 34,56,789'},
      {'rank': '5', 'name': 'LMN Brothers', 'value': '₹ 12,34,567'},
    ];
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            gradient: LinearGradient(colors: [Colors.blue[700]!, Colors.indigo[600]!]),
            borderRadius: BorderRadius.circular(16),
          ),
          child: const Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Top Performer', style: TextStyle(color: Colors.white70, fontSize: 12)),
              SizedBox(height: 4),
              Text('ABC Enterprises', style: TextStyle(color: Colors.white, fontSize: 22, fontWeight: FontWeight.bold)),
              Text('₹ 1,23,45,678', style: TextStyle(color: Colors.white, fontSize: 16)),
            ],
          ),
        ),
        const SizedBox(height: 16),
        ...items.map((item) => ListTile(
          leading: CircleAvatar(
            backgroundColor: item['rank'] == '1' ? Colors.amber.withValues(alpha: 0.2)
                : item['rank'] == '2' ? Colors.grey.withValues(alpha: 0.2)
                : Colors.brown.withValues(alpha: 0.2),
            child: Text(item['rank']!, style: TextStyle(fontWeight: FontWeight.bold, color: item['rank'] == '1' ? Colors.amber : Colors.grey)),
          ),
          title: Text(item['name']!, style: const TextStyle(fontWeight: FontWeight.w500, fontSize: 14)),
          trailing: Text(item['value']!, style: const TextStyle(fontWeight: FontWeight.bold)),
        )),
      ],
    );
  }

  Widget _expenseList() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _summaryRow('Total Expenses', '₹ 12,45,678'),
        const Divider(),
        _expenseRow('Office Rent', '₹ 3,50,000'),
        _expenseRow('Salaries', '₹ 5,20,000'),
        _expenseRow('Travel', '₹ 1,25,000'),
        _expenseRow('Utilities', '₹ 85,000'),
        _expenseRow('Marketing', '₹ 95,678'),
        _expenseRow('Miscellaneous', '₹ 70,000'),
      ],
    );
  }

  Widget _summaryRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        children: [
          Text(label, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          const Spacer(),
          Text(value, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: Colors.red)),
        ],
      ),
    );
  }

  Widget _expenseRow(String label, String value) {
    return ListTile(
      leading: Icon(Icons.circle, size: 8, color: Colors.blue[700]),
      title: Text(label, style: const TextStyle(fontSize: 14)),
      trailing: Text(value, style: const TextStyle(fontWeight: FontWeight.w500)),
      dense: true,
    );
  }

  Widget _inactiveList(BuildContext context, String type) {
    final items = type == 'customers'
        ? ['Crystal Distributors', 'Delta Supplies', 'Innovative Tech']
        : ['Sand (Fine)', 'Magnetic Tiles (Set)', 'CPVC Pipe 1 inch'];
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.orange.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Row(
            children: [
              Icon(Icons.warning_amber, color: Colors.orange[700]),
              const SizedBox(width: 12),
              Text('${items.length} Inactive $type', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.orange[800])),
            ],
          ),
        ),
        const SizedBox(height: 12),
        ...items.map((item) => ListTile(
          leading: CircleAvatar(
            backgroundColor: Colors.grey.withValues(alpha: 0.2),
            child: Icon(Icons.person_off, color: Colors.grey[600], size: 20),
          ),
          title: Text(item, style: const TextStyle(fontSize: 14)),
          trailing: TextButton(
            onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text('Reactivate $item')),
            ),
            child: const Text('Reactivate'),
          ),
        )),
      ],
    );
  }

  Widget _ledgerReport() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _ledgerRow('ABC Enterprises', '₹ 1,23,45,678', 'Dr', Colors.red),
        _ledgerRow('XYZ Industries', '₹ 56,78,901', 'Cr', Colors.green),
        _ledgerRow('PQR Tradelink', '₹ 7,89,01,234', 'Dr', Colors.red),
        _ledgerRow('LMN Brothers', '₹ 12,34,567', 'Cr', Colors.green),
        _ledgerRow('EFG Corporation', '₹ 3,45,67,890', 'Dr', Colors.red),
      ],
    );
  }

  Widget _ledgerRow(String name, String amount, String type, Color color) {
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 4),
      elevation: 0,
      color: Colors.grey[50],
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: color.withValues(alpha: 0.1),
          child: Text(type, style: TextStyle(fontWeight: FontWeight.bold, color: color, fontSize: 14)),
        ),
        title: Text(name, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w500)),
        trailing: Text(amount, style: const TextStyle(fontWeight: FontWeight.bold)),
      ),
    );
  }

  Widget _dayBook() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _dayEntry('Sales Invoice', 'ABC Enterprises', '₹ 25,000', '10:30 AM'),
        _dayEntry('Receipt', 'XYZ Industries', '₹ 12,000', '11:15 AM'),
        _dayEntry('Payment', 'PQR Tradelink', '₹ 8,500', '02:00 PM'),
        _dayEntry('Sales Invoice', 'LMN Brothers', '₹ 45,000', '03:30 PM'),
        _dayEntry('Receipt', 'EFG Corporation', '₹ 67,890', '04:45 PM'),
      ],
    );
  }

  Widget _dayEntry(String type, String party, String amount, String time) {
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 4),
      elevation: 0,
      color: Colors.grey[50],
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: Colors.blue.withValues(alpha: 0.1),
          child: Icon(Icons.receipt, color: Colors.blue[700], size: 20),
        ),
        title: Text(party, style: const TextStyle(fontWeight: FontWeight.w500, fontSize: 14)),
        subtitle: Text('$type • $time', style: TextStyle(fontSize: 11, color: Colors.grey[700])),
        trailing: Text(amount, style: const TextStyle(fontWeight: FontWeight.bold)),
      ),
    );
  }

  Widget _pendingList(String type) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _pendingRow('$type #001', 'ABC Enterprises', '25,000', '02 Jul 26'),
        _pendingRow('$type #002', 'XYZ Industries', '12,500', '05 Jul 26'),
        _pendingRow('$type #003', 'PQR Tradelink', '78,900', '10 Jul 26'),
        _pendingRow('$type #004', 'LMN Brothers', '34,000', '15 Jul 26'),
      ],
    );
  }

  Widget _pendingRow(String id, String party, String amount, String date) {
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 4),
      elevation: 0,
      color: Colors.grey[50],
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: Colors.orange.withValues(alpha: 0.1),
          child: Icon(Icons.pending, color: Colors.orange[700], size: 20),
        ),
        title: Row(
          children: [
            Text(id, style: const TextStyle(fontWeight: FontWeight.w500, fontSize: 14)),
            const Spacer(),
            Text('₹ $amount', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
          ],
        ),
        subtitle: Text('$party • Due: $date', style: TextStyle(fontSize: 11, color: Colors.grey[700])),
      ),
    );
  }

  Widget _pnlView() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _pnlRow('Sales Revenue', '₹ 45,67,890', Colors.black),
        _pnlRow('Other Income', '₹ 2,34,567', Colors.black),
        const Divider(),
        _pnlRow('Total Income', '₹ 48,02,457', Colors.green.shade700),
        const SizedBox(height: 16),
        _pnlRow('Cost of Goods Sold', '₹ 28,90,123', Colors.red.shade700),
        _pnlRow('Operating Expenses', '₹ 12,45,678', Colors.red.shade700),
        const Divider(),
        _pnlRow('Total Expenses', '₹ 41,35,801', Colors.red),
        const SizedBox(height: 16),
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.green.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Row(
            children: [
              const Icon(Icons.account_balance, color: Colors.green),
              const SizedBox(width: 12),
              const Text('Net Profit', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
              const Spacer(),
              Text('₹ 6,66,656', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: Colors.green[700])),
            ],
          ),
        ),
      ],
    );
  }

  Widget _pnlRow(String label, String value, Color color) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(
        children: [
          Expanded(child: Text(label, style: TextStyle(fontSize: 14, color: color))),
          Text(value, style: TextStyle(fontWeight: FontWeight.w600, color: color)),
        ],
      ),
    );
  }

  Widget _balanceSheet() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _sectionTitle('ASSETS'),
        _bsRow('Current Assets', '₹ 67,89,01,234'),
        _bsRow('Fixed Assets', '₹ 12,34,56,789'),
        _bsRow('Investments', '₹ 5,67,89,012'),
        const Divider(),
        _bsRow('Total Assets', '₹ 85,91,47,035', bold: true),
        const SizedBox(height: 24),
        _sectionTitle('LIABILITIES'),
        _bsRow('Current Liabilities', '₹ 34,56,78,901'),
        _bsRow('Long Term Debt', '₹ 12,34,56,789'),
        _divider2(),
        _bsRow('Total Liabilities', '₹ 46,91,35,690', bold: true),
        const SizedBox(height: 16),
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.blue.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Row(
            children: [
              const Icon(Icons.account_balance, color: Colors.blue),
              const SizedBox(width: 12),
              const Text("Shareholder's Equity", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
              const Spacer(),
              Text('₹ 39,00,11,345', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: Colors.blue[700])),
            ],
          ),
        ),
      ],
    );
  }

  Widget _sectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Text(title, style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14, color: Colors.blue[800])),
    );
  }

  Widget _bsRow(String label, String value, {bool bold = false}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Expanded(child: Text(label, style: TextStyle(fontSize: 13, fontWeight: bold ? FontWeight.bold : FontWeight.normal))),
          Text(value, style: TextStyle(fontWeight: bold ? FontWeight.bold : FontWeight.w500)),
        ],
      ),
    );
  }

  Widget _divider2() => const Divider(height: 1);

  void _showShare(BuildContext context) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Share $title report')),
    );
  }

  void _showOptions(BuildContext context) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('$title options')),
    );
  }
}
