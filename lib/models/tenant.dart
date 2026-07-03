class Tenant {
  final String id;
  final String code;
  final String name;
  final String? gstin;

  const Tenant({
    required this.id,
    required this.code,
    required this.name,
    this.gstin,
  });
}
