# Renovate + AI-fix op GitHub

Demo-repo bij de presentatie *"Dependency-updates die zichzelf repareren"*.

**Kort:** Renovate opent een PR voor een dependency-update. Breekt de build daardoor, dan start een tweede workflow een AI-agent. Die past de code aan, het script controleert de fix zelf, pusht hem naar de PR-branch en zet een samenvatting in de PR. Een mens reviewt en merget.

```
Renovate-app ──► PR "Update jjwt to v0.13.0" ──► workflow CI faalt (cannot find symbol)
                                                        │ workflow_run
                                                        ▼
                     ai-fix.yml: context ─► agent ─► mvn verify ─► vangrails ─► push + comment
                                                                                  │
                                                        CI draait opnieuw ◄───────┘ ─► groen ─► review
```

## Wat zit erin

| Pad | Wat |
| --- | --- |
| `src/` | Kleine Spring Boot 4.2-app (Java 21) die JWT's uitgeeft en controleert, met JUnit- en Cucumber-tests (Nederlandse Gherkin). |
| `pom.xml` | Gebruikt bewust **jjwt 0.11.5**. In jjwt 0.12.0 is `Jwts.parserBuilder()` verwijderd, dus de update naar 0.13.0 breekt precies één regel code. |
| `renovate.json` | Renovate-config voor deze repo. |
| `.github/workflows/ci.yml` | Build en test bij elke PR en elke push naar `main`. |
| `.github/workflows/ai-fix.yml` | Start als `CI` faalt op een PR van een `renovate/*`-branch, en laat de AI-agent de build repareren. |
| `scripts/ai-fix.sh` | Stuurt de AI-agent aan, in drie stappen: `prepare`, `agent`, `publish`. Werkt op GitHub én Azure DevOps. |
| `.ai/fix-instructions.md` | De instructies (prompt) voor de agent. |
| `azure-devops/` | Bijlage: dezelfde opzet voor Azure DevOps, met eigen README. |

## Stap 0: Claude-toegang

Kies één van deze drie.

| Optie | Zo regel je het | Secret in GitHub |
| --- | --- | --- |
| **Je eigen Claude-abonnement** (Pro of Max) | Installeer Claude Code op je eigen computer ([code.claude.com/docs](https://code.claude.com/docs)) en draai `claude setup-token`. Je logt in via de browser en krijgt een token dat een jaar geldig is. Het gebruik telt mee voor de limieten van je abonnement; je betaalt niets extra. | `CLAUDE_CODE_OAUTH_TOKEN` |
| Anthropic API-key | Maak een key op [platform.claude.com](https://platform.claude.com). Je betaalt per gebruik; het script zet een plafond (`AI_FIX_MAX_BUDGET_USD`). | `ANTHROPIC_API_KEY` |
| GitHub Copilot | Een fine-grained token met de permissie **Copilot Requests**, van een account met een Copilot-abonnement. Zet ook de variabele `AI_FIX_AGENT` op `copilot`. | `COPILOT_GITHUB_TOKEN` |

Zet je er meerdere, dan gebruikt het script de API-key vóór het abonnementstoken.

## Stap 1: GitHub klaarzetten

1. **Repo aanmaken.** Maak op GitHub een nieuwe, **publieke** repo, bijvoorbeeld `renovate-ai-demo`, zonder README. Publiek betekent onbeperkt gratis Actions-minuten; je secrets blijven geheim. Push deze code:

   ```bash
   cd renovate-ai-demo
   git init -b main
   git add .
   git commit -m "Demo-app voor Renovate + AI"
   git remote add origin https://github.com/<jouw-account>/renovate-ai-demo.git
   git push -u origin main
   ```

2. **Claude-secret.** *Settings → Secrets and variables → Actions → New repository secret*: het secret uit stap 0.

3. **Push-token.** Ga naar je profiel: *Settings → Developer settings → Personal access tokens → Fine-grained tokens → Generate new token*.
   - *Repository access*: **Only select repositories** → deze repo
   - *Permissions → Repository permissions → Contents*: **Read and write**
   - Een korte geldigheid (bijvoorbeeld 30 dagen) is prima.

   Zet het token als repository-secret **`AI_FIX_PUSH_TOKEN`**.
   *Waarom?* Een push met het standaard `GITHUB_TOKEN` start geen nieuwe workflow. Dan draait CI na de AI-fix niet opnieuw en blijft de PR rood.

4. **Check CI.** Tab *Actions*: de workflow **CI** op `main` moet groen zijn.

5. **Renovate-app installeren.** Ga naar [github.com/apps/renovate](https://github.com/apps/renovate) → *Install* → **Only select repositories** → deze repo. De app is gratis; volg zo nodig de aanmeldstappen van Mend voor het gratis Renovate-plan. Renovate ziet `renovate.json`, maakt een issue **Dependency Dashboard** en opent de PR *"Update jjwt to v0.13.0"*. Dat is meteen je eerste repetitie.

## Stap 2: generale repetitie (de dag ervoor)

1. Open de PR *"Update jjwt to v0.13.0"*. Bij *Checks* faalt **CI** met `cannot find symbol: method parserBuilder()`.
2. Tab *Actions*: de workflow **AI-fix voor Renovate-PR** start vanzelf.
3. Na een paar minuten staan er een commit van *AI Fix Bot* en een comment met de samenvatting in de PR, en wordt **CI** groen.
4. Meet hoe lang het duurt en **maak een schermopname**. Die is je vangnet tijdens de presentatie.
5. Reset de demo (zie hieronder).

## Draaiboek tijdens de presentatie

| Moment | Actie |
| --- | --- |
| Begin van je talk | Open het issue **Dependency Dashboard** en vink onderaan *"Check this box to trigger a request for Renovate to run again on this repository"* aan. Renovate maakt de PR opnieuw, en de rest loopt vanzelf: de "kookprogramma-truc". |
| Demo-blok | Laat achtereenvolgens zien: de PR-beschrijving van Renovate (versies en release notes), de rode **CI**-check met de compileerfout, de run van **AI-fix voor Renovate-PR** (stap 1, 2 en 3), de commit van *AI Fix Bot*, het comment en de groene **CI**. Review tot slot de diff hardop in *Files changed*. |
| Niet op tijd klaar? | Laat de workflow live lopen en schakel over naar de opname van de repetitie. |
| Bonus | Open `scripts/ai-fix.sh` en laat de vangrail in `publish` zien die het pushen weigert zodra de agent een bestand van Renovate aanraakt. |

## Demo resetten

Merge de PR tijdens repetities niet. Doe in plaats daarvan het volgende:

1. **Hernoem de PR-titel**, bijvoorbeeld door er ` [repetitie 1]` achter te zetten. Renovate herkent gesloten PR's aan hun titel. Een gesloten PR met de oorspronkelijke titel betekent voor Renovate: *deze update wil men niet*.
2. **Close** de PR (niet mergen) en klik op **Delete branch**.
3. Vink in het **Dependency Dashboard** de checkbox onderaan aan. Renovate maakt een verse PR.

## Instellingen

Variabelen zet je onder *Settings → Secrets and variables → Actions → Variables*. Ze zijn allemaal optioneel.

| Variabele | Standaard | Betekenis |
| --- | --- | --- |
| `AI_FIX_AGENT` | `claude` | `claude` of `copilot` |
| `AI_FIX_MAX_ATTEMPTS` | `2` | Maximaal aantal AI-commits per PR, daarna een comment "heeft een mens nodig" |
| `AI_FIX_MAX_TURNS` | `40` | Maximaal aantal agent-stappen (Claude) |
| `AI_FIX_MAX_BUDGET_USD` | `3` | Plafond per run op de geschatte kosten (Claude). Ook met een abonnement een nuttige rem. |
| `CLAUDE_MODEL` / `COPILOT_MODEL` | leeg | Model vastpinnen |
| `AI_FIX_ALLOW_DEPENDENCY_FILE_CHANGES` | `false` | Op `true` mag de agent ook bestanden aanpassen die Renovate wijzigde (bijvoorbeeld een extra dependency in `pom.xml`) |

## Hoe het je beschermt

| Stap | Heeft toegang tot | Doet |
| --- | --- | --- |
| `prepare` | `GITHUB_TOKEN` (lezen) | Haalt de branch, de diff, de buildfout en de PR-beschrijving op. Stopt na `AI_FIX_MAX_ATTEMPTS` pogingen. |
| `agent` | Alleen het Claude-token, **geen** GitHub-token | De agent mag lezen, bewerken en `mvn` draaien, maar niet committen of pushen. Daarna draait het script zelf `mvn verify`. |
| `publish` | Push-token en `GITHUB_TOKEN`, **geen** Claude-token | Weigert te pushen als er niets veranderd is, als de build nog faalt, of als de agent bestanden van Renovate aanpaste. Commit als *AI Fix Bot*, pusht en plaatst het comment. |

Verder:

- **Twee workflows.** `ci.yml` draait de code van de PR met alleen leesrechten. `ai-fix.yml` start via `workflow_run`, draait de code van `main` en neemt het script en de prompt van `main` mee. Een PR-branch kan de fix-tooling dus niet aanpassen.
- **Alleen eigen branches.** De AI-fix draait alleen voor `renovate/*`-branches in deze repo, nooit voor PR's uit forks.
- **Abonnementstoken.** Met `CLAUDE_CODE_OAUTH_TOKEN` draait Claude Code niet in `--bare`-modus, omdat die modus geen abonnementstokens leest. Het script weigert dan te starten als de branch `CLAUDE.md`, `.claude/` of `.mcp.json` bevat, zodat er geen onverwachte instructies of tools meekomen.
- **Renovate blijft eraf.** De fix-commit heeft een andere auteur dan Renovate, dus Renovate behandelt de branch als handmatig aangepast en overschrijft je fix niet.

## Veelvoorkomende fouten

| Wat je ziet | Oplossing |
| --- | --- |
| **AI-fix voor Renovate-PR** start niet | `workflow_run` werkt alleen voor workflows die op `main` staan, alleen als **CI** faalde, en alleen voor een PR van een `renovate/*`-branch in deze repo. |
| `AI_FIX_PUSH_TOKEN ontbreekt` | Stap 1.3 |
| Push faalt met 403 | Het push-token heeft geen *Contents: Read and write* op deze repo, of is verlopen. |
| `Geen Claude-toegang` | Stap 0 en 1.2: het secret ontbreekt of heeft een andere naam. |
| Claude geeft 401 met het abonnementstoken | Maak een nieuw token met `claude setup-token`, bijvoorbeeld na een wijziging van je abonnement. |
| Renovate maakt geen PR | Check of de app op deze repo staat, en gebruik de checkbox in het Dependency Dashboard. |

## Azure DevOps of GitLab?

- **Azure DevOps:** zie [`azure-devops/README.md`](azure-devops/README.md). Hetzelfde script, andere pipeline-bestanden.
- **GitLab:** het idee werkt hetzelfde. Let op dezelfde valkuil: een push met het ingebouwde `CI_JOB_TOKEN` start geen nieuwe pipeline, dus gebruik ook daar een eigen access token om te pushen.

## Lokaal draaien

```bash
mvn verify
mvn spring-boot:run
curl -s -X POST localhost:8080/api/tokens -H 'Content-Type: application/json' -d '{"subject":"alice"}'
curl -s -X POST localhost:8080/api/tokens/verify -H 'Content-Type: application/json' -d '{"token":"<token>"}'
```
