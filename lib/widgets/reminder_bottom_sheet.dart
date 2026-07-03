import 'package:flutter/material.dart';

enum _ReminderStep { main, autoSub }

class ReminderBottomSheet extends StatefulWidget {
  final ValueChanged<String>? onContinue;

  const ReminderBottomSheet({super.key, this.onContinue});

  @override
  State<ReminderBottomSheet> createState() => _ReminderBottomSheetState();
}

class _ReminderBottomSheetState extends State<ReminderBottomSheet> {
  _ReminderStep _step = _ReminderStep.main;
  int? _selected;
  int? _autoSubSelected;
  String? _mainSelection;

  void _selectAuto() {
    setState(() {
      _mainSelection = 'Auto Reminders';
      _step = _ReminderStep.autoSub;
    });
  }

  void _selectManual() {
    setState(() {
      _mainSelection = 'Manual Reminders';
      _selected = 1;
    });
  }

  void _back() {
    setState(() {
      _step = _ReminderStep.main;
      _autoSubSelected = null;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(
        left: 16,
        right: 16,
        top: 16,
        bottom: MediaQuery.of(context).viewInsets.bottom + 16,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              if (_step == _ReminderStep.autoSub)
                IconButton(
                  icon: const Icon(Icons.arrow_back),
                  onPressed: _back,
                ),
              Text(
                _step == _ReminderStep.main
                    ? 'Select Type of Reminder'
                    : 'Auto Reminders',
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
              ),
              IconButton(
                icon: const Icon(Icons.close),
                onPressed: () => Navigator.of(context).pop(),
              ),
            ],
          ),
          const SizedBox(height: 12),
          AnimatedSwitcher(
            duration: const Duration(milliseconds: 200),
            child: _step == _ReminderStep.main
                ? _mainStep()
                : _autoSubStep(),
          ),
          const SizedBox(height: 8),
          Text(
            'Reminders will be sent via email and SMS based on your configured schedule.',
            style: TextStyle(fontSize: 12, color: Colors.grey[600]),
          ),
          const SizedBox(height: 16),
          SizedBox(
            width: double.infinity,
            height: 48,
            child: FilledButton(
              onPressed: _canContinue() ? () {
                final type = _mainSelection == 'Manual Reminders' ? 'manual' : 'auto';
                Navigator.of(context).pop();
                widget.onContinue?.call(type);
              } : null,
              child: const Text('CONTINUE'),
            ),
          ),
        ],
      ),
    );
  }

  bool _canContinue() {
    if (_mainSelection == 'Auto Reminders') return _autoSubSelected != null;
    return _selected == 1;
  }

  Widget _mainStep() {
    return Column(
      children: [
        _optionCard(
          icon: Icons.autorenew,
          title: 'Auto Reminders',
          description: 'Automatically send reminders based on predefined rules and schedules.',
          isSelected: _mainSelection == 'Auto Reminders',
          onTap: _selectAuto,
          showBadge: true,
        ),
        const SizedBox(height: 8),
        _optionCard(
          icon: Icons.edit_outlined,
          title: 'Manual Reminders',
          description: 'Create and send reminders manually as needed.',
          isSelected: _mainSelection == 'Manual Reminders',
          onTap: _selectManual,
        ),
      ],
    );
  }

  Widget _autoSubStep() {
    return Column(
      children: [
        _optionCard(
          icon: Icons.notifications_active,
          title: 'Overdue Payment Reminder',
          description: 'Send automatic reminders for payments that are past due date.',
          isSelected: _autoSubSelected == 0,
          onTap: () => setState(() => _autoSubSelected = 0),
          showBadge: true,
        ),
        const SizedBox(height: 8),
        _optionCard(
          icon: Icons.schedule,
          title: 'Upcoming Payment Reminder',
          description: 'Notify customers before payment due dates.',
          isSelected: _autoSubSelected == 1,
          onTap: () => setState(() => _autoSubSelected = 1),
        ),
      ],
    );
  }

  Widget _optionCard({
    required IconData icon,
    required String title,
    required String description,
    required bool isSelected,
    required VoidCallback onTap,
    bool showBadge = false,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: isSelected ? Colors.blue[50] : Colors.grey[100],
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: isSelected ? Colors.blue : Colors.transparent,
            width: 2,
          ),
        ),
        child: Row(
          children: [
            Icon(icon, color: isSelected ? Colors.blue : Colors.grey),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Text(title, style: const TextStyle(fontWeight: FontWeight.w600)),
                      if (showBadge) ...[
                        const SizedBox(width: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                          decoration: BoxDecoration(
                            color: Colors.green,
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: const Text('Recommended',
                              style: TextStyle(color: Colors.white, fontSize: 9, fontWeight: FontWeight.w500)),
                        ),
                      ],
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(description, style: TextStyle(fontSize: 12, color: Colors.grey[600])),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
