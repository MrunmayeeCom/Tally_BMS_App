import 'package:flutter/material.dart';

class ReminderSchedulerScreen extends StatefulWidget {
  const ReminderSchedulerScreen({super.key});

  @override
  State<ReminderSchedulerScreen> createState() => _ReminderSchedulerScreenState();
}

class _ReminderSchedulerScreenState extends State<ReminderSchedulerScreen> {
  bool _autoReminderEnabled = true;
  bool _smsEnabled = true;
  bool _emailEnabled = true;
  bool _whatsappEnabled = false;
  bool _excludeHolidays = true;
  bool _sendOnDueDate = true;
  bool _sendBeforeDueDate = false;
  bool _sendAfterDueDate = true;
  int _reminderTimeHour = 10;
  int _reminderTimeMinute = 0;
  String _frequency = 'Daily';
  final Set<String> _selectedLedgers = {'ABC Enterprises', 'PQR Tradelink', 'EFG Corporation'};
  String _bankAccount = 'HDFC Bank - Current A/C';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.blue[700],
        foregroundColor: Colors.white,
        title: const Text('Auto Reminders Scheduler'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _toggleSection(
            'Auto Reminder',
            Icons.notifications_active,
            Switch(value: _autoReminderEnabled, onChanged: (v) => setState(() => _autoReminderEnabled = v)),
          ),
          _sectionHeader('Manage Contacts'),
          _optionTile('SMS', Icons.sms, _smsEnabled, (v) => setState(() => _smsEnabled = v)),
          _optionTile('Email', Icons.email, _emailEnabled, (v) => setState(() => _emailEnabled = v)),
          _optionTile('WhatsApp', Icons.chat, _whatsappEnabled, (v) => setState(() => _whatsappEnabled = v)),
          _sectionHeader('Reminder Time'),
          _timePicker(),
          _sectionHeader('Reminder Frequency'),
          _frequencySelector(),
          _sectionHeader('Select Ledgers'),
          _ledgerSelector(),
          _sectionHeader('Auto Reminder Report'),
          _optionTile('Send Report via Email', Icons.assessment, true, (_) {}),
          _sectionHeader('Customise Communication'),
          _communicationPreview(),
          _sectionHeader('Additional Settings'),
          _optionTile('Exclude Holidays', Icons.event_busy, _excludeHolidays, (v) => setState(() => _excludeHolidays = v)),
          _optionTile('Send on Due Date', Icons.calendar_today, _sendOnDueDate, (v) => setState(() => _sendOnDueDate = v)),
          _optionTile('Send Before Due Date', Icons.calendar_view_day, _sendBeforeDueDate, (v) => setState(() => _sendBeforeDueDate = v)),
          _optionTile('Send After Due Date', Icons.calendar_month, _sendAfterDueDate, (v) => setState(() => _sendAfterDueDate = v)),
          _sectionHeader('Default Bank Account'),
          _bankAccountSelector(),
          const SizedBox(height: 24),
          SizedBox(
            width: double.infinity,
            height: 48,
            child: FilledButton(
              onPressed: () {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Reminder settings saved')),
                );
                Navigator.of(context).pop();
              },
              child: const Text('SAVE SETTINGS'),
            ),
          ),
          const SizedBox(height: 16),
        ],
      ),
    );
  }

  Widget _sectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.only(top: 20, bottom: 8),
      child: Text(title,
          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: Colors.blue)),
    );
  }

  Widget _toggleSection(String title, IconData icon, Widget trailing) {
    return Card(
      child: ListTile(
        leading: Icon(icon, color: Colors.blue[700]),
        title: Text(title, style: const TextStyle(fontWeight: FontWeight.w600)),
        trailing: trailing,
      ),
    );
  }

  Widget _optionTile(String label, IconData icon, bool value, ValueChanged<bool> onChanged) {
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 3),
      child: SwitchListTile(
        secondary: Icon(icon, color: Colors.blue[700]),
        title: Text(label, style: const TextStyle(fontSize: 14)),
        value: value,
        onChanged: onChanged,
        dense: true,
      ),
    );
  }

  Widget _timePicker() {
    return Card(
      child: ListTile(
        leading: Icon(Icons.access_time, color: Colors.blue[700]),
        title: const Text('Reminder Time'),
        trailing: Text(
          '${_reminderTimeHour.toString().padLeft(2, '0')}:${_reminderTimeMinute.toString().padLeft(2, '0')}',
          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
        ),
        onTap: () async {
          final t = await showTimePicker(context: context, initialTime: TimeOfDay(hour: _reminderTimeHour, minute: _reminderTimeMinute));
          if (t != null) setState(() { _reminderTimeHour = t.hour; _reminderTimeMinute = t.minute; });
        },
      ),
    );
  }

  Widget _frequencySelector() {
    return Card(
      child: ListTile(
        leading: Icon(Icons.repeat, color: Colors.blue[700]),
        title: const Text('Frequency'),
        trailing: DropdownButton<String>(
          value: _frequency,
          underline: const SizedBox(),
          items: ['Daily', 'Weekly', 'Monthly', 'Custom']
              .map((f) => DropdownMenuItem(value: f, child: Text(f)))
              .toList(),
          onChanged: (v) => setState(() => _frequency = v!),
        ),
      ),
    );
  }

  Widget _ledgerSelector() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.receipt_long, color: Colors.blue[700], size: 20),
                const SizedBox(width: 8),
                const Text('Select Ledgers', style: TextStyle(fontWeight: FontWeight.w600)),
                const Spacer(),
                TextButton.icon(
                  icon: const Icon(Icons.add, size: 18),
                  label: const Text('Add'),
                  onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Add ledgers')),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Wrap(
              spacing: 6,
              runSpacing: 4,
              children: _selectedLedgers.map((l) => Chip(
                label: Text(l, style: const TextStyle(fontSize: 12)),
                deleteIcon: const Icon(Icons.close, size: 16),
                onDeleted: () => setState(() => _selectedLedgers.remove(l)),
              )).toList(),
            ),
          ],
        ),
      ),
    );
  }

  Widget _communicationPreview() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.message, color: Colors.blue[700], size: 20),
                const SizedBox(width: 8),
                const Text('Message Template', style: TextStyle(fontWeight: FontWeight.w600)),
                const Spacer(),
                TextButton(
                  onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Edit message template')),
                  ),
                  child: const Text('Edit'),
                ),
              ],
            ),
            const SizedBox(height: 4),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.grey[100],
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(
                'Dear {Party},\nYour payment of {Amount} is due on {DueDate}.\nPlease make the payment at your earliest convenience.\n\nThank you,\nBMS Tally',
                style: TextStyle(fontSize: 12, color: Colors.grey[700]),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _bankAccountSelector() {
    return Card(
      child: ListTile(
        leading: Icon(Icons.account_balance, color: Colors.blue[700]),
        title: const Text('Default Account'),
        trailing: DropdownButton<String>(
          value: _bankAccount,
          underline: const SizedBox(),
          items: ['HDFC Bank - Current A/C', 'ICICI Bank - Savings A/C', 'SBI - Current A/C']
              .map((a) => DropdownMenuItem(value: a, child: Text(a, style: const TextStyle(fontSize: 13))))
              .toList(),
          onChanged: (v) => setState(() => _bankAccount = v!),
        ),
      ),
    );
  }
}
