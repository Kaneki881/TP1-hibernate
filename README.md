# TP1 Hibernate

A small Java project that shows the basics of JPA with Hibernate, using an in-memory H2 database. It covers entity mapping, a one-to-many relationship, and simple create/read/update/delete operations.

## Technologies

- Java (8 or newer, 11 recommended)
- Maven
- Hibernate Core 5.6.5.Final (JPA 2.2 implementation)
- H2 Database 2.1.214 (in-memory)
- SLF4J Simple (logging)

## Project structure

```
tp1-hibernate
├── pom.xml
└── src
    ├── main
    │   ├── java/com/example
    │   │   ├── App.java                 # Demo: insert, read, update, delete, search
    │   │   └── model
    │   │       ├── Categorie.java       # Entity: a category with its products
    │   │       └── Produit.java         # Entity: a product (id, nom, prix, categorie)
    │   └── resources/META-INF
    │       └── persistence.xml          # Persistence unit "hibernate-demo" (H2)
    └── test/java/com/example
        └── AppTest.java
```

## Data model

- **Produit**: `id` (generated), `nom`, `prix` (`BigDecimal`), and a `categorie`.
- **Categorie**: `id` (generated), `nom`, and a list of `produits`.

The relationship is bidirectional. `Produit` owns it with `@ManyToOne`, so the foreign key `categorie_id` lives in the product table. `Categorie` is the inverse side with `@OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL)`, so saving a category also saves its products. The helper methods `ajouterProduit` and `retirerProduit` keep both sides in sync.

## What the demo does

`App.main` runs these steps in order:

1. Creates two categories ("Informatique" and "Multimédia") with four products and saves them through their categories.
2. Lists all products, finds one by ID, and lists each category with its products (`JOIN FETCH`).
3. Updates the price of the Smartphone (`mettreAJourPrix`), using Hibernate's dirty checking.
4. Searches products priced between 300 and 600 (`rechercherParPlagePrix`, JPQL `BETWEEN`).
5. Deletes the Tablette (`supprimerProduit`), after detaching it from its category.
6. Lists the products again to show the result.

## Configuration

The persistence unit is defined in `src/main/resources/META-INF/persistence.xml`:

- H2 in-memory database `jdbc:h2:mem:testdb`, user `sa`, empty password
- `hibernate.hbm2ddl.auto=create-drop`: tables are created at startup and dropped at shutdown
- `hibernate.show_sql=true` and `hibernate.format_sql=true`: generated SQL is printed in the console

Because the database lives in memory, all data is lost when the program ends.

## Build and run

```bash
mvn compile exec:java -Dexec.mainClass=com.example.App
```

Notes:

- `pom.xml` does not set a Java compiler level. If compilation fails on the `<>` diamond syntax, add `maven.compiler.source` and `maven.compiler.target` (for example `11`) to the `<properties>` of `pom.xml`.
- `AppTest.java` uses JUnit, which is not declared in `pom.xml`. Add the `junit` dependency before running `mvn test` or `mvn package`.

## Possible next steps

- Add a `Client` entity and a many-to-many relationship with `Produit`
- Move the persistence code out of `App` into DAO/repository classes
- Add real JUnit tests for the CRUD methods
