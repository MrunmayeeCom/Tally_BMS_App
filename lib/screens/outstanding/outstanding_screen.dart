import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:bmstally_app/providers/app_provider.dart';
import 'package:bmstally_app/widgets/reminder_bottom_sheet.dart';
import 'package:bmstally_app/screens/reminder_scheduler_screen.dart';
import 'package:bmstally_app/screens/manual_reminder_selection_screen.dart';

class OutstandingScreen extends StatefulWidget {
  const OutstandingScreen({super.key});

  @override
  State<OutstandingScreen> createState() => _OutstandingScreenState();
}

class _OutstandingScreenState extends State<OutstandingScreen>
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
    final provider = context.watch<AppProvider>();

    return Column(
      children: [
        _appBar(context),
        _summaryHeader(provider),
        _reminderBanner(),
        _tabBar(provider),
        Expanded(
          child: TabBarView(
            controller: _tabController,
            children: [
              _ledgerList(provider),
              _groupList(provider),
            ],
          ),
        ),
      ],
    );
  }

  Widget _appBar(BuildContext context) {
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
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('Outstanding',
                    style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w500)),
                Text('Manage receivables & payables',
                    style: TextStyle(color: Colors.white70, fontSize: 12)),
              ],
            ),
          ),
          IconButton(
            icon: const Icon(Icons.search, color: Colors.white),
            onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Search outstanding ledgers')),
            ),
          ),
          IconButton(
            icon: const Icon(Icons.tune, color: Colors.white),
            onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Filter options')),
            ),
          ),
          IconButton(
            icon: const Icon(Icons.more_vert, color: Colors.white),
            onPressed: () async {
              final messenger = ScaffoldMessenger.of(context);
              final v = await showMenu<String>(
                context: context,
                position: RelativeRect.fromLTRB(1000, 80, 0, 0),
                items: const [
                  PopupMenuItem(value: 'export', child: Text('Export')),
                  PopupMenuItem(value: 'print', child: Text('Print')),
                  PopupMenuItem(value: 'refresh', child: Text('Refresh')),
                ],
              );
              if (v != null && context.mounted) {
                messenger.showSnackBar(SnackBar(content: Text('$v selected')));
              }
            },
          ),
        ],
      ),
    );
  }

  Widget _summaryHeader(AppProvider provider) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      color: Colors.blue[50],
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Total Outstanding',
               style: TextStyle(color: Colors.grey[800], fontSize: 13)),
          const SizedBox(height: 4),
          Text(provider.outstandingSummary,
              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 24)),
          const SizedBox(height: 4),
          Text('Last updated: 18 Jun 26 @ 05:40 pm',
               style: TextStyle(color: Colors.grey[700], fontSize: 11)),
        ],
      ),
    );
  }

  Widget _reminderBanner() {
    return Container(
      margin: const EdgeInsets.fromLTRB(16, 12, 16, 0),
      decoration: BoxDecoration(
        color: Colors.indigo,
        borderRadius: BorderRadius.circular(12),
      ),
      child: ListTile(
        leading: const Icon(Icons.notifications_outlined, color: Colors.white),
        title: const Text('Manage Auto Reminders',
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.w600)),
        trailing: const Icon(Icons.chevron_right, color: Colors.white),
        onTap: () {
          showModalBottomSheet(
            context: context,
            isScrollControlled: true,
            shape: const RoundedRectangleBorder(
              borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
            ),
            builder: (_) => ReminderBottomSheet(
              onContinue: (type) {
                if (type == 'manual') {
                  Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const ManualReminderSelectionScreen()),
                  );
                } else {
                  Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const ReminderSchedulerScreen()),
                  );
                }
              },
            ),
          );
        },
      ),
    );
  }

  Widget _tabBar(AppProvider provider) {
    return Container(
      color: Colors.blue[50],
      child: Row(
        children: [
          Expanded(
            child: TabBar(
              controller: _tabController,
              labelColor: Colors.blue[700],
              unselectedLabelColor: Colors.grey,
              indicatorColor: Colors.blue[700],
              tabs: const [
                Tab(text: 'Ledgers'),
                Tab(text: 'Groups'),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.only(right: 8),
            child: IconButton(
              icon: Icon(Icons.bar_chart, color: Colors.blue[700]),
              onPressed: () {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Chart view')),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _ledgerList(AppProvider provider) {
    final items = provider.outstandingLedgers;
    return ListView.builder(
      padding: const EdgeInsets.symmetric(vertical: 8),
      itemCount: items.length,
      itemBuilder: (_, i) {
        final item = items[i];
        return Card(
          margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
          elevation: 0,
          color: Colors.grey[50],
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            child: Row(
              children: [
                CircleAvatar(
                  backgroundColor: item.isCredit
                      ? Colors.green.withValues(alpha: 0.1)
                      : Colors.red.withValues(alpha: 0.1),
                  child: Icon(
                    item.isCredit ? Icons.arrow_upward : Icons.arrow_downward,
                    color: item.isCredit ? Colors.green : Colors.red,
                    size: 20,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(item.partyName,
                          style: const TextStyle(fontWeight: FontWeight.w600)),
                      const SizedBox(height: 2),
                      Text('${item.creditInfo} | ${item.meta}',
                           style: TextStyle(fontSize: 11, color: Colors.grey[700])),
                    ],
                  ),
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(item.amount,
                        style: const TextStyle(fontWeight: FontWeight.bold)),
                    Text(item.paymentInfo,
                        style: TextStyle(fontSize: 11, color: Colors.grey[700])),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _groupList(AppProvider provider) {
    final groups = provider.outstandingGroups;
    return ListView.builder(
      padding: const EdgeInsets.symmetric(vertical: 8),
      itemCount: groups.length,
      itemBuilder: (_, i) {
        return Card(
          margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
          elevation: 0,
          color: Colors.grey[50],
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
          child: ListTile(
            leading: CircleAvatar(
              backgroundColor: Colors.blue.withValues(alpha: 0.1),
              child: const Icon(Icons.folder, color: Colors.blue),
            ),
            title: Text(groups[i][0],
                style: const TextStyle(fontWeight: FontWeight.w600)),
            trailing: Text(groups[i][1],
                style: const TextStyle(fontWeight: FontWeight.bold)),
          ),
        );
      },
    );
  }
}
