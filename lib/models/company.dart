class Company {
  final String id;
  final String name;
  final String gstin;

  const Company({
    required this.id,
    required this.name,
    this.gstin = '',
  });
}
