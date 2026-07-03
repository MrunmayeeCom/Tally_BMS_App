import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:bmstally_app/providers/app_provider.dart';
import 'package:bmstally_app/screens/reminder_scheduler_screen.dart';

class ManualReminderSelectionScreen extends StatefulWidget {
  const ManualReminderSelectionScreen({super.key});

  @override
  State<ManualReminderSelectionScreen> createState() => _ManualReminderSelectionScreenState();
}

class _ManualReminderSelectionScreenState extends State<ManualReminderSelectionScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final Set<String> _selectedParties = {};

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

  void _toggleParty(String name) {
    setState(() {
      if (_selectedParties.contains(name)) {
        _selectedParties.remove(name);
      } else {
        _selectedParties.add(name);
      }
    });
  }

  void _selectAll(Iterable<String> names) {
    setState(() => _selectedParties.addAll(names));
  }

  void _cancelSelection() {
    setState(() => _selectedParties.clear());
  }

  void _showConfirmation() {
    if (_selectedParties.isEmpty) return;
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (_) => _confirmationSheet(),
    );
  }

  Widget _confirmationSheet() {
    return Padding(
      padding: const EdgeInsets.all(20),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text('Send Reminders?',
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
              IconButton(
                icon: const Icon(Icons.close),
                onPressed: () => Navigator.of(context).pop(),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            'Are you sure you want to send manual reminders to ${_selectedParties.length} selected customer(s)?',
            style: TextStyle(fontSize: 14, color: Colors.grey[600]),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 4,
            children: _selectedParties
                .map((p) => Chip(
                      label: Text(p, style: const TextStyle(fontSize: 12)),
                      materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                    ))
                .toList(),
          ),
          const SizedBox(height: 20),
          SizedBox(
            width: double.infinity,
            height: 48,
            child: FilledButton.icon(
              onPressed: () {
                Navigator.of(context).pop();
                Navigator.of(context).pop();
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Reminders sent to ${_selectedParties.length} customer(s)')),
                );
              },
              icon: const Icon(Icons.send),
              label: const Text('SEND REMINDER'),
            ),
          ),
          const SizedBox(height: 8),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<AppProvider>();
    final ledgers = provider.outstandingLedgers;
    final groups = provider.outstandingGroups;

    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.blue[700],
        foregroundColor: Colors.white,
        title: const Text('Manual Reminder'),
      ),
      body: Column(
        children: [
          _autoReminderBanner(),
          _tabBar(),
          Expanded(
            child: TabBarView(
              controller: _tabController,
              children: [
                _ledgerList(ledgers.map((l) => l.partyName).toList()),
                _groupList(groups.map((g) => g[0]).toList()),
              ],
            ),
          ),
          _bottomBar(),
        ],
      ),
    );
  }

  Widget _autoReminderBanner() {
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
          Navigator.of(context).push(
            MaterialPageRoute(builder: (_) => const ReminderSchedulerScreen()),
          );
        },
      ),
    );
  }

  Widget _tabBar() {
    return Container(
      color: Colors.grey[100],
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
    );
  }

  Widget _ledgerList(List<String> names) {
    if (names.isEmpty) {
      return const Center(child: Text('No ledgers found'));
    }
    return ListView.builder(
      padding: const EdgeInsets.symmetric(vertical: 4),
      itemCount: names.length,
      itemBuilder: (_, i) => _checkableTile(names[i]),
    );
  }

  Widget _groupList(List<String> names) {
    if (names.isEmpty) {
      return const Center(child: Text('No groups found'));
    }
    return ListView.builder(
      padding: const EdgeInsets.symmetric(vertical: 4),
      itemCount: names.length,
      itemBuilder: (_, i) => _checkableTile(names[i]),
    );
  }

  Widget _checkableTile(String name) {
    final selected = _selectedParties.contains(name);
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 3),
      elevation: 0,
      color: Colors.grey[50],
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
      child: CheckboxListTile(
        value: selected,
        onChanged: (_) => _toggleParty(name),
        title: Text(name, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w500)),
        secondary: CircleAvatar(
          backgroundColor: Colors.blue.withValues(alpha: 0.1),
          child: Icon(Icons.person, color: Colors.blue[700], size: 20),
        ),
        controlAffinity: ListTileControlAffinity.trailing,
        dense: true,
      ),
    );
  }

  Widget _bottomBar() {
    final count = _selectedParties.length;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(color: Colors.black.withValues(alpha: 0.1), blurRadius: 4, offset: const Offset(0, -2)),
        ],
      ),
          child: Row(
            children: [
              Expanded(
                child: OutlinedButton(
                  onPressed: () {
                    final p = context.read<AppProvider>();
                    _selectAll([
                      ...p.outstandingLedgers.map((l) => l.partyName),
                      ...p.outstandingGroups.map((g) => g[0]),
                    ]);
                  },
                  child: const Text('Select All'),
                ),
              ),
          const SizedBox(width: 12),
          Expanded(
            child: OutlinedButton(
              onPressed: _cancelSelection,
              child: const Text('Cancel'),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            flex: 2,
            child: FilledButton(
              onPressed: count > 0 ? _showConfirmation : null,
              child: Text(count > 0 ? 'Send ($count)' : 'Send'),
            ),
          ),
        ],
      ),
    );
  }
}
