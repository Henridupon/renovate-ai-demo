# Bijlage: dezelfde opzet op Azure DevOps

De demo draait op GitHub, maar het script `scripts/ai-fix.sh` werkt ook op Azure DevOps. Het verschil zit in de "lijm" eromheen: Renovate draai je zelf als pipeline, en de AI-fix is een extra stage in je PR-pipeline.

| | GitHub (de demo) | Azure DevOps (deze map) |
| --- | --- | --- |
| Renovate | Gratis Mend-app, twee klikken | Zelf draaien: `renovate.yml`, geplande pipeline |
| Release notes in de PR | Automatisch | Alleen met een GitHub-token (`GITHUB_COM_TOKEN`) |
| PR-build | Workflow `CI` op `pull_request` | Branch policy op `main` met pipeline `ci.yml` |
| AI-fix start | Aparte workflow via `workflow_run` | Stage `AiFix` in dezelfde pipeline |
| Pushen van de fix | Eigen token nodig (`AI_FIX_PUSH_TOKEN`) | `System.AccessToken` volstaat |
| Dependency Dashboard | Issue met checkboxes | Work item (vraagt extra rechten) |

## Claude-toegang

Zet één van deze als **secret** pipeline-variabele op de `ci`-pipeline:

| Optie | Variabele(n) |
| --- | --- |
| Je eigen Claude-abonnement (Pro of Max), via `claude setup-token` | `CLAUDE_CODE_OAUTH_TOKEN` |
| Anthropic API-key | `ANTHROPIC_API_KEY` |
| Claude via Microsoft Foundry: kijk op [ai.azure.com](https://ai.azure.com) of er in je Foundry-resource Claude-deployments staan, of vraag je cloudteam | `CLAUDE_CODE_USE_FOUNDRY` = `1`, `ANTHROPIC_FOUNDRY_RESOURCE`, `ANTHROPIC_FOUNDRY_API_KEY` (secret), `CLAUDE_MODEL` = naam van je deployment |
| GitHub Copilot | `AI_FIX_AGENT` = `copilot` en `COPILOT_GITHUB_TOKEN` (secret, fine-grained PAT met **Copilot Requests**) |

Werk je met bedrijfscode, check dan eerst bij security of die naar de gekozen AI-dienst mag.

## Opzetten

1. **Project en repo.** Maak een project met een lege Git-repo en push deze code naar `main`.
2. **Pipeline-capaciteit.** *Organization settings → Pipelines → Parallel jobs*. Staat Microsoft-hosted op 0? Nieuwe organisaties krijgen het gratis tegoed niet altijd automatisch. Vraag het aan via [aka.ms/azpipelines-parallelism-request](https://aka.ms/azpipelines-parallelism-request) en reken op een paar werkdagen.
3. **Rechten voor de build-service.** *Project settings → Repositories → (repo) → Security* → **`<projectnaam> Build Service (<organisatie>)`** → op *Allow*: Contribute, Create branch, Contribute to pull requests.
4. **Twee pipelines.** *Pipelines → New pipeline → Azure Repos Git → (repo) → Existing Azure Pipelines YAML file*:
   - `/azure-devops/ci.yml`: hernoemen naar **ci**
   - `/azure-devops/renovate.yml`: hernoemen naar **renovate**
5. **Branch policy op `main`.** *Project settings → Repositories → (repo) → Policies → main → Build validation → +*: pipeline **ci**, trigger *Automatic*, requirement *Required*.
6. **Variabelen.** Op **ci**: de Claude-toegang hierboven. Op **renovate** (aanbevolen): `GITHUB_COM_TOKEN` als secret, een fine-grained GitHub-token met alleen leesrechten op publieke repo's. Zonder dit token staan er geen release notes in de PR.
7. **Dependency Dashboard.** Op Azure DevOps maakt Renovate daar een work item van. Wil je dat niet, zet dan in `renovate.json`: `"dependencyDashboard": { "enabled": false }`.
8. **Eerste run.** Draai **ci** handmatig op `main`. Die moet groen worden.

Tijdens de demo start je de pipeline **renovate** handmatig aan het begin van je talk.

**Demo resetten:** hernoem de PR-titel, kies **Abandon**, verwijder de branch `renovate/jjwt` en draai **renovate** opnieuw.

## Veelvoorkomende fouten

| Melding | Oplossing |
| --- | --- |
| `No hosted parallelism has been purchased or granted` | Stap 2 |
| `TF401027: You need the Git 'GenericContribute' permission` | Stap 3 |
| Renovate klaagt over de Node-versie | Renovate 44 vereist Node 24. Dat regelt de `UseNode@1`-stap. |
| `Geen Claude-toegang` | De secret-variabele ontbreekt of heeft een andere naam. |
| Stage AiFix wordt overgeslagen | Die draait alleen bij een **PR-build** via de branch policy, van een `renovate/*`-branch, en alleen als stage Build faalde. |
| PR zonder release notes | `GITHUB_COM_TOKEN` ontbreekt (stap 6). |
