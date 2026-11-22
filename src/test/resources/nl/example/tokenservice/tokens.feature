# language: nl
Functionaliteit: Tokens uitgeven en controleren

  Scenario: Een uitgegeven token hoort bij de juiste gebruiker
    Gegeven een tokenservice met een geldig geheim
    Als er een token wordt uitgegeven voor "alice"
    Dan hoort het token bij "alice"

  Scenario: Een aangepast token wordt geweigerd
    Gegeven een tokenservice met een geldig geheim
    En er is een token uitgegeven voor "alice"
    Als iemand het token aanpast
    Dan wordt het token geweigerd
