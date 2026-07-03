import 'package:flutter/material.dart';

class FollowUpsScreen extends StatefulWidget {
  const FollowUpsScreen({super.key});

  @override
  State<FollowUpsScreen> createState() => _FollowUpsScreenState();
}

class _FollowUpsScreenState extends State<FollowUpsScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.blue[700],
        foregroundColor: Colors.white,
        title: const Text('Follow Ups'),
        actions: [
          IconButton(icon: const Icon(Icons.add), onPressed: () {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('New follow-up reminder')),
            );
          }),
        ],
      ),
      body: Column(
        children: [
          Container(
            color: Colors.grey[100],
            child: TabBar(
              controller: _tabController,
              labelColor: Colors.blue[700],
              unselectedLabelColor: Colors.grey,
              indicatorColor: Colors.blue[700],
              tabs: const [
                Tab(text: 'Upcoming'),
                Tab(text: 'Completed'),
              ],
            ),
          ),
          Expanded(
            child: TabBarView(
              controller: _tabController,
              children: [
                _list(true),
                _list(false),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _list(bool upcoming) {
    final items = upcoming
        ? [
            ['ABC Enterprises', 'Payment follow-up', 'Due in 2 days'],
            ['XYZ Industries', 'Quote confirmation', 'Due tomorrow'],
            ['PQR Tradelink', 'Contract renewal', 'Due in 5 days'],
            ['LMN Brothers', 'Product demo', 'Due in 1 week'],
          ]
        : [
            ['EFG Corporation', 'Invoice sent', 'Completed 2 days ago'],
            ['Amit Traders', 'Order confirmed', 'Completed yesterday'],
            ['Bharat Electronics', 'Meeting done', 'Completed 3 days ago'],
          ];

    if (items.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.task_alt, size: 64, color: Colors.grey[300]),
            const SizedBox(height: 12),
            Text('No follow-ups', style: TextStyle(color: Colors.grey[700], fontSize: 16)),
          ],
        ),
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: items.length,
      itemBuilder: (_, i) {
        final item = items[i];
        return Card(
          margin: const EdgeInsets.symmetric(vertical: 4),
          elevation: 0,
          color: Colors.grey[50],
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          child: ListTile(
            leading: CircleAvatar(
              backgroundColor: Colors.indigo.withValues(alpha: 0.1),
              child: Icon(upcoming ? Icons.pending : Icons.check_circle, color: Colors.indigo, size: 22),
            ),
            title: Text(item[0], style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14)),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(item[1], style: TextStyle(fontSize: 12, color: Colors.grey[700])),
                Text(item[2], style: TextStyle(fontSize: 11, color: upcoming ? Colors.orange : Colors.green)),
              ],
            ),
            trailing: upcoming
                ? TextButton(
                    onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('Reminder set for ${item[0]}')),
                    ),
                    child: const Text('Remind'),
                  )
                : null,
          ),
        );
      },
    );
  }
}
