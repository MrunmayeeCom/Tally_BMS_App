import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:bmstally_app/providers/app_provider.dart';
import 'package:bmstally_app/screens/report_detail_screen.dart';
import 'package:bmstally_app/screens/reminder_scheduler_screen.dart';

class ReportsScreen extends StatelessWidget {
  const ReportsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<AppProvider>();
    final reports = provider.reports;

    return Column(
      children: [
        _appBar(context),
        Expanded(
          child: ListView.separated(
            padding: const EdgeInsets.symmetric(vertical: 8),
            itemCount: reports.length,
            separatorBuilder: (_, _) =>
                const Divider(height: 1, indent: 16, endIndent: 16),
            itemBuilder: (_, i) {
              final title = reports[i].title;
              return ListTile(
                leading: Icon(Icons.description_outlined,
                    color: Colors.blue[700]),
                title: Text(title),
                trailing: const Icon(Icons.chevron_right, color: Colors.grey),
                onTap: () {
                  if (title == 'Auto Reminders') {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                          builder: (_) => const ReminderSchedulerScreen()),
                    );
                  } else {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                          builder: (_) => ReportDetailScreen(title: title)),
                    );
                  }
                },
              );
            },
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
          const Expanded(
            child: Text('Reports',
                style: TextStyle(
                    color: Colors.white,
                    fontSize: 18,
                    fontWeight: FontWeight.w500)),
          ),
        ],
      ),
    );
  }
}
