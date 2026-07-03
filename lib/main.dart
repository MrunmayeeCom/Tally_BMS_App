import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:bmstally_app/providers/app_provider.dart';
import 'package:bmstally_app/providers/items_provider.dart';
import 'package:bmstally_app/services/mock_data_service.dart';
import 'package:bmstally_app/screens/login_screen.dart';
import 'package:bmstally_app/screens/home_screen.dart';
import 'package:bmstally_app/screens/drawer_pages/companies_page.dart';
import 'package:bmstally_app/screens/drawer_pages/users_page.dart';
import 'package:bmstally_app/screens/drawer_pages/settings_page.dart';
import 'package:bmstally_app/screens/drawer_pages/wallet_page.dart';
import 'package:bmstally_app/screens/drawer_pages/subscription_page.dart';
import 'package:bmstally_app/screens/drawer_pages/help_page.dart';
import 'package:bmstally_app/screens/drawer_pages/about_page.dart';

void main() {
  MockDataService().seed();
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AppProvider()),
        ChangeNotifierProvider(create: (_) => ItemsProvider()),
      ],
      child: const BmsTallyApp(),
    ),
  );
}

class BmsTallyApp extends StatelessWidget {
  const BmsTallyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'BMS Tally',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.indigo),
        useMaterial3: true,
      ),
      initialRoute: '/login',
      routes: {
        '/login': (_) => const LoginScreen(),
        '/home': (_) => HomeScreen(),
        '/companies': (_) => const CompaniesPage(),
        '/users': (_) => const UsersPage(),
        '/settings': (_) => const SettingsPage(),
        '/wallet': (_) => const WalletPage(),
        '/subscription': (_) => const SubscriptionPage(),
        '/help': (_) => const HelpPage(),
        '/about': (_) => const AboutPage(),
      },
    );
  }
}
