import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:bmstally_app/providers/app_provider.dart';
import 'package:bmstally_app/widgets/custom_drawer.dart';
import 'package:bmstally_app/widgets/reminder_bottom_sheet.dart';
import 'package:bmstally_app/screens/dashboard/dashboard_screen.dart';
import 'package:bmstally_app/screens/outstanding/outstanding_screen.dart';
import 'package:bmstally_app/screens/sales_team/sales_team_screen.dart';
import 'package:bmstally_app/screens/reports/reports_screen.dart';
import 'package:bmstally_app/screens/create_entry_screen.dart';
import 'package:bmstally_app/screens/reminder_scheduler_screen.dart';
import 'package:bmstally_app/screens/manual_reminder_selection_screen.dart';

class HomeScreen extends StatelessWidget {
  HomeScreen({super.key});

  final _screens = <Widget>[
    const DashboardScreen(),
    const OutstandingScreen(),
    const SalesTeamScreen(),
    const ReportsScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<AppProvider>();

    return Scaffold(
      drawer: const CustomDrawer(),
      body: _screens[provider.tabIndex],
      floatingActionButton: _fab(context, provider.tabIndex),
      bottomNavigationBar: NavigationBar(
        selectedIndex: provider.tabIndex,
        onDestinationSelected: provider.setTab,
        destinations: const [
          NavigationDestination(
              icon: Icon(Icons.grid_view_outlined),
              selectedIcon: Icon(Icons.grid_view),
              label: 'Dashboard'),
          NavigationDestination(
              icon: Icon(Icons.receipt_long_outlined),
              selectedIcon: Icon(Icons.receipt_long),
              label: 'Outstanding'),
          NavigationDestination(
              icon: Icon(Icons.people_outline),
              selectedIcon: Icon(Icons.people),
              label: 'Sales Team'),
          NavigationDestination(
              icon: Icon(Icons.assessment_outlined),
              selectedIcon: Icon(Icons.assessment),
              label: 'Reports'),
        ],
      ),
    );
  }

  Widget? _fab(BuildContext context, int tab) {
    switch (tab) {
      case 0:
        return FloatingActionButton.extended(
          backgroundColor: Colors.orange,
          foregroundColor: Colors.white,
          onPressed: () {
            Navigator.of(context).push(
              MaterialPageRoute(builder: (_) => const CreateEntryScreen()),
            );
          },
          icon: const Icon(Icons.calendar_today),
          label: const Text('CREATE ENTRY'),
        );
      case 1:
        return FloatingActionButton.extended(
          backgroundColor: Colors.orange,
          foregroundColor: Colors.white,
          onPressed: () {
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
          icon: const Icon(Icons.notifications_active),
          label: const Text('Reminders'),
        );
      default:
        return null;
    }
  }
}
