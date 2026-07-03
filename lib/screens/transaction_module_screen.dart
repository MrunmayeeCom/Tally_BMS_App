import 'package:flutter/material.dart';
import 'package:bmstally_app/screens/create_transaction_screen.dart';

class TransactionModuleScreen extends StatefulWidget {
  final String title;

  const TransactionModuleScreen({super.key, required this.title});

  @override
  State<TransactionModuleScreen> createState() => _TransactionModuleScreenState();
}

class _TransactionModuleScreenState extends State<TransactionModuleScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  String _groupFilter = 'All';
  int _pendingCount = 0;
  int _successCount = 0;
  int _failedCount = 0;

  static const _amounts = {
    'Sales Invoice': '₹ 12,37,41,37,96,173',
    'Sales Order': '₹ 4,56,78,901',
    'Receipt': '₹ 98,765',
    'Delivery Note': '₹ 3,10,000',
    'Quotation': '₹ 8,75,000',
    'Purchase Invoice': '₹ 10,61,34,78,07,790',
    'Purchase Order': '₹ 3,45,67,890',
    'Payment': '₹ 76,543',
    'Receipt Note': '₹ 2,45,000',
    'Journal': '₹ 12,34,567',
    'Ledger': '₹ 5,67,89,012',
  };

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
    _loadCounts();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  void _loadCounts() {
    final hash = widget.title.hashCode;
    _pendingCount = (hash % 7) + 1;
    _successCount = (hash % 13) + 3;
    _failedCount = hash % 5;
  }

  String get _totalAmount => _amounts[widget.title] ?? '₹ 0';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.blue[700],
        foregroundColor: Colors.white,
        title: Text(widget.title),
        actions: [
          IconButton(
            icon: const Icon(Icons.date_range),
            onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text('Change date range for ${widget.title}')),
            ),
          ),
          IconButton(
            icon: const Icon(Icons.filter_list),
            onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text('Filter ${widget.title}')),
            ),
          ),
          IconButton(
            icon: const Icon(Icons.settings),
            onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text('${widget.title} settings')),
            ),
          ),
        ],
      ),
      body: Column(
        children: [
          _summaryCard(),
          _dateRange(),
          _groupDropdown(),
          _tabBar(),
          Expanded(child: _tabContent()),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        backgroundColor: Colors.orange,
        foregroundColor: Colors.white,
        onPressed: () {
          Navigator.of(context).push(
            MaterialPageRoute(
              builder: (_) => CreateTransactionScreen(type: widget.title),
            ),
          );
        },
        child: const Icon(Icons.add),
      ),
    );
  }

  Widget _summaryCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      color: Colors.blue[50],
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Total ${widget.title}',
               style: TextStyle(color: Colors.grey[800], fontSize: 13)),
          const SizedBox(height: 4),
          Text(_totalAmount,
              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 24)),
          const SizedBox(height: 4),
          Row(
            children: [
              _statusChip('Pending', _pendingCount, Colors.orange),
              const SizedBox(width: 8),
              _statusChip('Success', _successCount, Colors.green),
              const SizedBox(width: 8),
              _statusChip('Failed', _failedCount, Colors.red),
            ],
          ),
        ],
      ),
    );
  }

  Widget _statusChip(String label, int count, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text('$label $count',
          style: TextStyle(fontSize: 11, fontWeight: FontWeight.w600, color: color)),
    );
  }

  Widget _dateRange() {
    return Container(
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

  Widget _groupDropdown() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: DropdownButtonFormField<String>(
        initialValue: _groupFilter,
        decoration: const InputDecoration(
          labelText: 'Group',
          prefixIcon: Icon(Icons.category),
          border: OutlineInputBorder(),
          contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          isDense: true,
        ),
        items: ['All', 'Group A', 'Group B', 'Group C']
            .map((g) => DropdownMenuItem(value: g, child: Text(g, style: const TextStyle(fontSize: 13))))
            .toList(),
        onChanged: (v) => setState(() => _groupFilter = v!),
      ),
    );
  }

  Widget _tabBar() {
    return Container(
      margin: const EdgeInsets.only(top: 8),
      color: Colors.grey[100],
      child: TabBar(
        controller: _tabController,
        labelColor: Colors.blue[700],
        unselectedLabelColor: Colors.grey,
        indicatorColor: Colors.blue[700],
        tabs: const [
          Tab(text: 'Pending'),
          Tab(text: 'Success'),
          Tab(text: 'Failed'),
        ],
      ),
    );
  }

  Widget _tabContent() {
    final isEmpty = _pendingCount + _successCount + _failedCount == 0;
    if (isEmpty) return _emptyState();
    return TabBarView(
      controller: _tabController,
      children: [
        _tabList('Pending', Icons.pending),
        _tabList('Success', Icons.check_circle),
        _tabList('Failed', Icons.cancel),
      ],
    );
  }

  Widget _tabList(String status, IconData icon) {
    final count = _tabController.index == 0
        ? _pendingCount
        : _tabController.index == 1
            ? _successCount
            : _failedCount;
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: count,
      itemBuilder: (_, i) => Card(
        margin: const EdgeInsets.symmetric(vertical: 4),
        elevation: 0,
        color: Colors.grey[50],
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        child: ListTile(
          leading: CircleAvatar(
            backgroundColor: Colors.blue.withValues(alpha: 0.1),
            child: Icon(icon, color: Colors.blue[700], size: 20),
          ),
          title: Text('${widget.title} #${i + 1}',
              style: const TextStyle(fontWeight: FontWeight.w500, fontSize: 14)),
          subtitle: Text(status, style: TextStyle(fontSize: 11, color: Colors.grey[700])),
          trailing: Text('₹ ${(i + 1) * 1000}',
              style: const TextStyle(fontWeight: FontWeight.bold)),
        ),
      ),
    );
  }

  Widget _emptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.inbox_outlined, size: 72, color: Colors.grey[300]),
          const SizedBox(height: 16),
          Text('No ${widget.title}',
               style: TextStyle(fontSize: 16, color: Colors.grey[700])),
          const SizedBox(height: 4),
          Text('There are no records to show.',
               style: TextStyle(fontSize: 13, color: Colors.grey[600])),
        ],
      ),
    );
  }
}
