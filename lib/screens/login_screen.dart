import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:bmstally_app/providers/app_provider.dart';
import 'package:bmstally_app/providers/items_provider.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _tenantController = TextEditingController(text: 'tally');
  final _usernameController = TextEditingController(text: 'admin');
  final _passwordController = TextEditingController();
  bool _obscurePassword = true;
  bool _loading = false;

  @override
  void dispose() {
    _tenantController.dispose();
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _login() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _loading = true);

    final provider = context.read<AppProvider>();
    final success = await provider.login(
      _tenantController.text.trim(),
      _usernameController.text.trim(),
      _passwordController.text,
    );

    setState(() => _loading = false);

    if (success && mounted) {
      context.read<ItemsProvider>().setTenantId(provider.tenantId);
      Navigator.of(context).pushReplacementNamed('/home');
    } else if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Invalid tenant, username or password')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 32),
          child: Form(
            key: _formKey,
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.calculate_rounded, size: 80,
                    color: Theme.of(context).colorScheme.primary),
                const SizedBox(height: 16),
                Text('BMS Tally',
                    style: Theme.of(context).textTheme.headlineLarge?.copyWith(
                        fontWeight: FontWeight.bold)),
                const SizedBox(height: 8),
                Text('Sign in to your account',
                    style: TextStyle(color: Colors.grey[700])),
                const SizedBox(height: 32),
                TextFormField(
                  controller: _tenantController,
                  decoration: const InputDecoration(
                    labelText: 'Tenant Code',
                    prefixIcon: Icon(Icons.business),
                    border: OutlineInputBorder(),
                    helperText: 'e.g. tally, bmscorp, techmart',
                  ),
                  validator: (v) => v == null || v.trim().isEmpty ? 'Enter tenant code' : null,
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _usernameController,
                  decoration: const InputDecoration(
                    labelText: 'Username',
                    prefixIcon: Icon(Icons.person_outline),
                    border: OutlineInputBorder(),
                  ),
                  validator: (v) => v == null || v.isEmpty ? 'Enter username' : null,
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _passwordController,
                  obscureText: _obscurePassword,
                  decoration: InputDecoration(
                    labelText: 'Password',
                    prefixIcon: const Icon(Icons.lock_outline),
                    border: const OutlineInputBorder(),
                    suffixIcon: IconButton(
                      icon: Icon(_obscurePassword ? Icons.visibility_off : Icons.visibility),
                      onPressed: () => setState(() => _obscurePassword = !_obscurePassword),
                    ),
                  ),
                  validator: (v) => v == null || v.isEmpty ? 'Enter password' : null,
                ),
                const SizedBox(height: 24),
                SizedBox(
                  width: double.infinity,
                  height: 48,
                  child: FilledButton(
                    onPressed: _loading ? null : _login,
                    child: _loading
                        ? const SizedBox(
                            width: 20, height: 20,
                            child: CircularProgressIndicator(strokeWidth: 2))
                        : const Text('Login'),
                  ),
                ),
                const SizedBox(height: 12),
                Text(
                  'Demo: use any tenant code shown above\nusername: admin, password: admin...',
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 11, color: Colors.grey[600]),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
