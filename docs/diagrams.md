``` merdaid
flowchart TB
  subgraph PSS [Send Ships]
    D1(J2 Send Ships) --> B1(J1 Send Ships)
    B1 --> D1
  end

  subgraph InGame [In Game]
    F1(J2 Send Send Salvos) --> G1(J1 Send Send Salvos)
    G1 --> F1
  end

  A[Created Gane] --> B(J1 Send Ships)
  B --> C(J2 Joined Game)
  C --> D(J2 Send Ships)
  D --> InGame

  A[Created Gane] --> C1(J2 Joined Game)
  C1 --> PSS
  PSS --> InGame

```
