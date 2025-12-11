Perfect! Now I have comprehensive information about Kafka Streams patterns, team size considerations for WIP limits, and OpenAPI for microservices communication. Let me update the document with the specific team details and technology stack.

# **DevOps Deployment & Updates Guide**

**Version:** 2.0  
**Last Updated:** December 2025  
**Audience:** Development, Operations, Release Management Teams  
**Tools Stack:** Slack, Telegram, Jira, Confluence, Kubernetes, Java Microservices, PostgreSQL, Mobile Apps (iOS/Android), Website, **HTTP/REST APIs, OpenAPI 3.0+, Apache Kafka, Kafka Streams**

**Team Structure:**
- **Retail Team** (3 backend developers) - Larger, complex domain
- **Core Team** (2 backend developers) - Larger, complex domain  
- **AML Team** (2 backend developers) - Smaller, specialized domain
- **Redesign Team** (1 backend developer) - Smaller, UI-focused domain

***

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Team Structure & Kanban Workflow](#team-structure--kanban-workflow)
3. [Cross-Team Coordination & Communication](#cross-team-coordination--communication)
4. [API Development Standards (OpenAPI Required)](#api-development-standards-openapi-required)
5. [Kafka & Event-Driven Communication](#kafka--event-driven-communication)
6. [Deployment Process Overview](#deployment-process-overview)
7. [Git Branching Strategy & Feature Stand Deployment](#git-branching-strategy--feature-stand-deployment)
8. [Release Pipeline & Automation](#release-pipeline--automation)
9. [Database Migrations Strategy](#database-migrations-strategy)
10. [Release Checklist & Management](#release-checklist--management)
11. [Incident Management Process](#incident-management-process)
12. [Post-Incident Review (Postmortem)](#post-incident-review-postmortem)
13. [Tools & Integration Recommendations](#tools--integration-recommendations)
14. [Secure Git Data Practices](#secure-git-data-practices)
15. [Knowledge Documentation Standards](#knowledge-documentation-standards)

***

## Executive Summary

This guide addresses critical organizational challenges across four cross-functional teams (Retail, Core, AML, Redesign) working on the same microservices platform using Kanban methodology.

### Technology Stack

**Backend Communication:**
- **HTTP/REST APIs**: Synchronous inter-service communication using OpenAPI 3.0+ specifications[1][2][3]
- **Apache Kafka**: Asynchronous event-driven messaging for decoupled service communication[4][5][6]
- **Kafka Streams**: Real-time stream processing for event sourcing and data transformation[6][7]

**Core Problems Addressed:**

1. **Knowledge Centralization Risk**: Single-person dependencies eliminated through documentation
2. **Uncontrolled Deployments**: Mandatory quality gates with automated enforcement
3. **Concurrent Team Conflicts**: 4 teams modifying same services simultaneously
4. **API Versioning Chaos**: Mobile apps can't deploy quickly; strict versioning required
5. **Missing Release Process**: No formal checklists, ownership, or historical tracking
6. **Incident Response Gaps**: No structured response or blameless postmortem culture

**Key Principles:**
- **API-First Development**: All APIs defined with OpenAPI specs before implementation
- **Event-Driven Architecture**: Kafka for asynchronous communication and event sourcing
- **Continuous Flow**: Kanban methodology with WIP limits and daily coordination
- **Backward Compatibility**: Mandatory for mobile clients with slow deployment cycles
- **Automation First**: Remove manual decision points, enforce with CI/CD
- **Transparency**: All deployments, incidents, and API changes are visible and traceable

***

## Team Structure & Kanban Workflow

### Team Composition & Workload Distribution

| **Team** | **Backend Developers** | **Complexity** | **Focus Area** | **WIP Limit Calculation** |
|----------|----------------------|----------------|----------------|---------------------------|
| **Retail** | 3 devs | High | Customer-facing features, checkout, catalog | Team Size × 1.5 = **4-5 items** |
| **Core** | 2 devs | High | Authentication, payments, user management | Team Size × 1.5 = **3 items** |
| **AML** | 2 devs | Medium | Anti-money laundering, compliance | Team Size × 1.5 = **3 items** |
| **Redesign** | 1 dev | Low-Medium | UI modernization, frontend integration | Team Size × 2.0 = **2 items** |

**WIP Limit Formula**:[8][9]
```
Optimal WIP = (Team Size × Base Factor) × Complexity Modifier

Base Factor: 1.5-2.0 for complex work, 2.0-2.5 for simpler work
Complexity Modifier: 1.0 for high complexity teams, 1.2 for medium complexity
```

### Single Kanban Board Structure

**Board Name:** `Microservices Platform`

```
Swimlanes (by Team):
├── Retail Team (3 devs) - High complexity
├── Core Team (2 devs) - High complexity  
├── AML Team (2 devs) - Medium complexity
└── Redesign Team (1 dev) - Low-Medium complexity

Columns (Workflow States):
[Backlog] → [Ready] → [API Design] → [In Progress] → [Code Review] → [Testing] → [Done]
  ∞           12         8              10              6               8          -

WIP Limits per Column (Total across all teams):
- Backlog: No limit (prioritized by Product Owner)
- Ready: 12 items (3 per team average)
- API Design: 8 items (OpenAPI spec creation phase)
- In Progress: 10 items (Retail:4-5, Core:3, AML:3, Redesign:2)
- Code Review: 6 items (force fast reviews to prevent bottleneck)
- Testing: 8 items (QA capacity)
- Done: No limit (archived weekly)
```

**Why Single Board with Swimlanes?**[10][11][8]

- **Unified Priority**: Top items across all teams visible at once
- **Visual Conflict Detection**: See when Retail and Core both touch user-service
- **Balanced Workload**: Prevent Retail from hogging all WIP while Redesign waits
- **Cross-Team Dependencies**: Visible when AML depends on Core's payment changes
- **Fair Resource Allocation**: Smaller teams (Redesign) not overshadowed by larger teams (Retail)

### Team-Specific WIP Limits

**Per-Team WIP Limits (In Progress Column)**:[9][12][13][8]

```
Retail Team (3 devs): 4-5 items max
├── 3 × 1.5 = 4.5 (high complexity work)
├── Developer 1: 1-2 items
├── Developer 2: 1-2 items
└── Developer 3: 1-2 items

Core Team (2 devs): 3 items max
├── 2 × 1.5 = 3 (high complexity, critical services)
├── Developer 1: 1-2 items
└── Developer 2: 1-2 items

AML Team (2 devs): 3 items max
├── 2 × 1.5 = 3 (medium complexity, compliance focus)
├── Developer 1: 1-2 items
└── Developer 2: 1-2 items

Redesign Team (1 dev): 2 items max
├── 1 × 2.0 = 2 (simpler UI work, less backend complexity)
└── Developer 1: 1-2 items
```

**Rule:** No team can exceed their WIP limit. If limit reached, **help other teams** or **clear code reviews** before pulling new work.[14][9]

### Classes of Service (Priority Tiers)

Tag all Jira issues with Class of Service to guide pull decisions:[15]

| **Class** | **Label** | **Color** | **SLA** | **Example** | **% of WIP** |
|-----------|-----------|-----------|---------|-------------|--------------|
| **Expedite** | `expedite` | Red | < 24 hours | Production incident, security vulnerability, payment outage | < 5% |
| **Fixed Date** | `fixed-date` | Orange | Specific deadline | Mobile app store release, regulatory deadline, partner integration | 15-20% |
| **Standard** | `standard` | Blue | Normal flow | New features, enhancements, non-critical bugs | 60-70% |
| **Intangible** | `intangible` | Green | No deadline | Tech debt, refactoring, documentation, infrastructure improvements | 15-20% |

**Priority Rules:**
1. **Expedite** preempts all other work; team swarms to resolve
2. **Fixed Date** items escalate as deadline approaches
3. **Standard** flows normally based on business value
4. **Intangible** work prevented from accumulation (dedicate 15-20% capacity)

### Kanban Daily Standup (15 minutes, 9:00 AM UTC)

**Format:** All 4 teams participate (8 backend devs total)

**Agenda per team (3 min each):**

1. **What we're pulling today** (Backlog → Ready → API Design → In Progress)
2. **Service locks** (Which services are we actively modifying?)
3. **What we're releasing today** (Testing → Done, merging to main)
4. **Blockers** (Dependencies on other teams, waiting for API contracts)

**Slack Template (Posted Daily in `#kanban-sync`):**

```markdown
📋 **Daily Kanban Sync - December 9, 2025**

**Retail Team** (3 devs, WIP: 4/5):
✅ Pulling: RETAIL-456 (product-catalog-service/search optimization)  
🔒 Active Services: product-catalog-service (In Code Review), checkout-service (In Progress)
🚀 Releasing: RETAIL-432 (cart-service/add wishlist feature)
❌ Blocked: None

**Core Team** (2 devs, WIP: 3/3):
✅ Pulling: None (at WIP limit, focusing on current items)
🔒 Active Services: user-service/authentication (In Progress), payment-service (Code Review)
🚀 Releasing: CORE-678 (auth-service/OAuth2 integration)
❌ Blocked: CORE-681 waiting on Retail's product-catalog API contract

**AML Team** (2 devs, WIP: 2/3):
✅ Pulling: AML-234 (transaction-monitor-service/new risk rules)
🔒 Active Services: transaction-monitor-service (In Progress)
🚀 Releasing: AML-220 (compliance-report-service/PDF export)
❌ Blocked: None

**Redesign Team** (1 dev, WIP: 1/2):
✅ Pulling: REDESIGN-123 (API integration for new checkout UI)
🔒 Active Services: None (API Design phase for new work)
🚀 Releasing: None
❌ Blocked: REDESIGN-123 waiting for Retail's checkout-service API spec (RETAIL-456)

**⚠️ Conflicts Detected:**
- **user-service/authentication**: Core Team actively working (CORE-678)
  - Redesign team planning work on user-service/profile (REDESIGN-130) - coordinate before starting
- **payment-service**: Core Team in Code Review (CORE-675)
  - No conflicts expected this week

**📊 Board Health:**
- Total WIP: 10/10 items (at capacity ✅)
- Code Review queue: 4/6 (healthy ✅)
- Testing queue: 5/8 (healthy ✅)
- Oldest item: RETAIL-401 (8 days in Testing - needs attention ⚠️)
```

***

## Cross-Team Coordination & Communication

### Service Lock Coordination

**Problem:** Retail Team and Core Team both need to modify `user-service` authentication this week.

**Solution: First-to-Start Service Lock Protocol**[16][17][18]

**Step 1: Announce Intent in Daily Standup**

Team announces: "We plan to start work on [service]/[module] today"

**Step 2: Visual Lock in Jira**

Add label to ticket: `lock:user-service-auth`

**Step 3: Service Lock Rules**

| **Scenario** | **Resolution** | **Example** |
|--------------|----------------|-------------|
| **Same service, different modules** | Both proceed independently | Core touches auth, Retail touches profile ✅ |
| **Same service, same module** | First team to move to "In Progress" gets lock. Second team waits or coordinates | Core starts auth work Monday → Retail waits or pairs with Core ⏸️ |
| **Expedite work** | Expedite preempts Standard work | Production auth bug (Expedite) → Core pauses Standard work, swarms to fix 🚨 |
| **Fixed Date collision** | Product Owner prioritizes | Retail's mobile release (Dec 15) vs Core's partner integration (Dec 20) → PO decides |

**Step 4: Pair Programming Option**

Instead of waiting, teams can **pair program** to complete work faster:

```
Core Team + Retail Team → Joint session on user-service auth
- Core dev leads (owns the lock)
- Retail dev assists (learns module, contributes)
- Result: Faster completion, knowledge sharing, no waiting
```

### CODEOWNERS File for Review Enforcement

Create `.github/CODEOWNERS` in repository root:[19][20][21]

```plaintext
# Global fallback
* @platform-lead

# Service-level ownership (teams own specific services)
/services/user-service/ @core-team-backend
/services/auth-service/ @core-team-backend
/services/payment-service/ @core-team-backend

/services/product-catalog-service/ @retail-team-backend
/services/cart-service/ @retail-team-backend
/services/checkout-service/ @retail-team-backend
/services/order-service/ @retail-team-backend

/services/transaction-monitor-service/ @aml-team-backend
/services/compliance-report-service/ @aml-team-backend

/services/api-gateway/ @redesign-team-backend @platform-lead

# Shared components require multi-team approval
/shared/auth-library/ @core-team-backend @retail-team-backend
/shared/payment-library/ @core-team-backend @aml-team-backend
/shared/api-contracts/ @retail-team-backend @core-team-backend @aml-team-backend @redesign-team-backend

# Kafka topics and schemas (multi-team impact)
/kafka/topics/ @platform-lead @core-team-backend @retail-team-backend
/kafka/schemas/ @platform-lead @core-team-backend @retail-team-backend

# Database migrations - requires DBA approval
**/migrations/*.sql @dba-team @platform-lead

# Infrastructure
/.github/workflows/ @devops-team @platform-lead
/k8s/ @devops-team @platform-lead
/helm/ @devops-team
```

**Code Review Rules:**

| **Change Type** | **Required Approvals** | **Example** |
|-----------------|----------------------|-------------|
| **Single-team service** | 1 approval from same team | Retail modifies cart-service → 1 Retail dev review |
| **Cross-team service** | 1 approval from each affected team | Core modifies user-service used by Retail → 1 Core + 1 Retail approval |
| **Shared library** | 2 approvals from different teams | Auth library change → 1 Core + 1 Retail approval |
| **Kafka topic/schema** | Platform lead + 1 team lead | New Kafka topic → Platform lead + affected team approval |
| **Database migration** | DBA + platform lead | Schema change → DBA + platform lead approval |
| **API contract change** | All consuming teams | Payment API v2 → Core + Retail + AML approval |

***

## API Development Standards (OpenAPI Required)

### Technology Stack: HTTP/REST + OpenAPI

All inter-service communication uses **HTTP/REST APIs** with **OpenAPI 3.0+ specifications**.[2][3][1]

**Why OpenAPI for Microservices?**[1]

- **Contract-First Development**: Define API before implementation, teams develop in parallel
- **Client Code Generation**: Auto-generate Java/JavaScript/Swift client libraries
- **Documentation**: Auto-generate interactive docs (Swagger UI, Redoc)
- **Contract Testing**: Validate provider-consumer compatibility
- **Breaking Change Detection**: Automated tools catch incompatible changes

### Mandatory API Versioning for Mobile Compatibility

**Problem:** Mobile apps (iOS/Android) require 3-14 days for App Store approval. Web deploys instantly. APIs must support multiple versions simultaneously.[22][23][24]

**Versioning Strategy: URI Path Versioning**

```
Mobile app v2.1 (released 3 months ago, 40% users):
  → https://api.example.com/v2/users

Mobile app v3.0 (released last week, 15% users):
  → https://api.example.com/v3/users

Web app (deployed today):
  → https://api.example.com/v3/users
```

**Semantic Versioning Rules**:[25][26][27]

| **Version Change** | **When to Use** | **Example** |
|--------------------|-----------------|-------------|
| **MAJOR** (v2 → v3) | Breaking changes, removes fields, changes types | Removed `fullName`, added `firstName`/`lastName` |
| **MINOR** (v2.1 → v2.2) | Backward-compatible additions | Added optional `phoneNumber` field |
| **PATCH** (v2.1.3 → v2.1.4) | Bug fixes, performance, security (non-breaking) | Fixed timezone conversion bug |

**Version Support Policy:**

| **Version** | **Support Duration** | **SLA** | **Deprecation Notice** |
|-------------|---------------------|---------|------------------------|
| **Current (v3)** | Indefinite | Full support, new features | N/A |
| **N-1 (v2)** | 6 months after v3 launch | Bug fixes only | 3 months advance notice |
| **N-2 (v1)** | 3 months after v2 sunset | Security fixes only | 6 months total notice |
| **Deprecated** | Disabled | Returns 410 Gone | 9 months total notice |

**Deprecation Timeline Example:**

```
Jan 2025: v3 released
  ✅ v3 = Current (full support)
  ⚠️ v2 = N-1 (bug fixes, deprecation notice sent)
  ⚠️ v1 = N-2 (security only)

July 2025: v2 sunset
  ✅ v3 = Current
  ❌ v2 = Disabled (returns 410 Gone)
  ⚠️ v1 = N-2 (3 months remaining)

Oct 2025: v1 sunset
  ✅ v3 = Current
  ❌ v1 = Disabled
```

### Definition of Ready (DoR) for API Tasks

Before moving Jira ticket from **Backlog → Ready**, MUST have:[28][29][30]

#### **1. OpenAPI 3.0+ Specification (Mandatory)**[31][32][33]

Create spec in `docs/openapi/{service}-{version}.yaml`:

```yaml
# docs/openapi/user-service-v3.yaml
openapi: 3.0.0
info:
  title: User Service API
  version: 3.0.0
  description: User management and authentication
  contact:
    name: Core Team
    email: core-team@example.com

servers:
  - url: https://api.example.com/v3
    description: Production
  - url: https://api-staging.example.com/v3
    description: Staging

paths:
  /users/{userId}:
    get:
      summary: Get user by ID
      operationId: getUserById
      tags:
        - Users
      parameters:
        - name: userId
          in: path
          required: true
          schema:
            type: integer
            format: int64
          description: Unique user identifier
      responses:
        '200':
          description: User found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserV3'
        '404':
          description: User not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

components:
  schemas:
    UserV3:
      type: object
      required:
        - userId
        - email
        - firstName
        - lastName
      properties:
        userId:
          type: integer
          format: int64
          example: 12345
        email:
          type: string
          format: email
          example: user@example.com
        firstName:
          type: string
          example: John
        lastName:
          type: string
          example: Doe
        phoneNumber:
          type: string
          example: "+1234567890"
          description: Optional phone number (added in v3.1)
        createdAt:
          type: string
          format: date-time
          example: "2025-01-15T10:30:00Z"
    
    Error:
      type: object
      required:
        - code
        - message
      properties:
        code:
          type: string
          example: "USER_NOT_FOUND"
        message:
          type: string
          example: "User with ID 12345 not found"
        timestamp:
          type: string
          format: date-time
```

**Validation:** Use Swagger Editor (https://editor.swagger.io) or Spectral CLI.[34][35]

```bash
# Validate OpenAPI spec
spectral lint docs/openapi/user-service-v3.yaml
```

#### **2. API Versioning Decision**

Ticket must document:

```markdown
## API Versioning Decision

**Change Type:** MAJOR (v2 → v3)

**Reasoning:** Removing deprecated `fullName` field and splitting into `firstName` + `lastName`.
This is breaking because v2 mobile clients expect `fullName` in response.

**Backward Compatibility Strategy:**
- v2 remains active for 6 months (sunset: July 1, 2026)
- v3 launched January 15, 2026
- Mobile app v3.0 submitted to App Store: January 20, 2026
- Web immediately uses v3
- Email notification to API consumers: January 1, 2026

**Migration Path:**
1. Deploy v3 alongside v2 (both active)
2. Mobile apps update to v3 over 6 months
3. Monitor v2 usage in Datadog
4. When v2 usage < 1%, schedule sunset
5. Disable v2 on July 1, 2026
```

#### **3. Affected Teams & Dependencies**

```markdown
## Teams Impacted

**Provider Team:** Core Team (owns user-service)

**Consumer Teams:**
- ✅ **Retail Team**: Uses user-service for customer profiles in checkout
  - Services: checkout-service, cart-service
  - Impact: Must update API client to handle v3 response
  - Coordination: Retail lead notified, joint API review scheduled

- ✅ **AML Team**: Uses user-service for KYC checks
  - Services: transaction-monitor-service
  - Impact: Low (only reads userId and email, both unchanged)
  - Coordination: AML lead notified via Slack

- ✅ **Redesign Team**: Updating UI to display firstName/lastName separately
  - Services: Frontend web app
  - Impact: High (UI depends on new fields)
  - Coordination: Already planned, part of Redesign scope

**External Dependencies:**
- [ ] Mobile iOS app (Team 3) - Update required
- [ ] Mobile Android app (Team 3) - Update required
- [ ] Partner API integration (external) - Migration guide required
```

#### **4. Testable Acceptance Criteria**

```markdown
## Acceptance Criteria

**Given** a mobile client on v2.5
**When** they call GET /v2/users/123
**Then** they receive `fullName` field (backward compatible)

**Given** a mobile client on v3.0
**When** they call GET /v3/users/123  
**Then** they receive `firstName` and `lastName` fields

**Given** an API consumer calls deprecated v1 endpoint
**When** response received
**Then** response includes deprecation headers:
  - X-API-Deprecated: true
  - X-API-Sunset-Date: 2026-07-01
  - Sunset: Wed, 01 Jul 2026 00:00:00 GMT

**Given** a service-to-service call (checkout-service → user-service)
**When** checkout-service calls user-service v3
**Then** response time < 200ms (p95)
**And** circuit breaker does not trip
```

#### **5. Security & Performance Baseline**

```markdown
## Security Review

- [x] Reviewed by Security Team: @security-lead (Dec 1, 2025)
- [x] PII handling compliant with GDPR
- [x] Rate limiting configured: 1000 req/min per API key
- [x] Authentication: OAuth 2.0 required (unchanged from v2)
- [x] Authorization: User can only access own profile (unchanged)

## Performance SLA

- **p50 latency:** < 50ms
- **p95 latency:** < 200ms
- **p99 latency:** < 500ms
- **Throughput:** > 2000 req/sec
- **Error rate:** < 0.1%
```

**DoR Sign-Off:** Product Owner + Tech Lead approve before "Ready".[29][28]

***

### Definition of Done (DoD) for API Tasks

Before moving from **Testing → Done**, MUST have:[30][36][29]

#### **1. OpenAPI Spec Published**

- [x] OpenAPI spec matches actual implementation (no drift)
- [x] Spec validated with Swagger Validator or Spectral
- [x] Spec published to API docs portal (Swagger UI at https://api-docs.example.com)
- [x] Spec checked into Git (`docs/openapi/user-service-v3.yaml`)

**CI/CD Validation:**

```yaml
# .github/workflows/api-spec-validation.yml
name: Validate OpenAPI Specs
on: [pull_request]

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Validate OpenAPI 3.0 specs
        uses: char0n/swagger-editor-validate@v1
        with:
          definition-file: docs/openapi/user-service-v3.yaml
      
      - name: Lint with Spectral
        run: |
          npm install -g @stoplight/spectral-cli
          spectral lint docs/openapi/user-service-v3.yaml
      
      - name: Check for breaking changes
        uses: oasdiff/oasdiff-action@v0.0.15
        with:
          base: docs/openapi/user-service-v2.yaml
          revision: docs/openapi/user-service-v3.yaml
          fail-on-breaking: true
```

#### **2. API Contract Tests Pass**[37][38][39]

**Consumer Contract Test (Retail Team - checkout-service):**

```java
// Retail team defines expectation from user-service
@Pact(consumer = "checkout-service", provider = "user-service")
public RequestResponsePact getUserById(PactDslWithProvider builder) {
    return builder
        .given("user 12345 exists")
        .uponReceiving("request for user 12345")
            .path("/v3/users/12345")
            .method("GET")
            .headers("Authorization", "Bearer token123")
        .willRespondWith()
            .status(200)
            .body(new PactDslJsonBody()
                .integerType("userId", 12345)
                .stringType("email", "user@example.com")
                .stringType("firstName", "John")
                .stringType("lastName", "Doe")
                .stringType("phoneNumber", "+1234567890")
            )
        .toPact();
}
```

**Provider Contract Verification (Core Team - user-service):**

```java
// Core team verifies they satisfy Retail's contract
@PactVerification(value = "checkout-service", fragment = "getUserById")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class UserServicePactVerificationTest {
    
    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }
    
    @BeforeEach
    void setup(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", 8080));
    }
    
    @State("user 12345 exists")
    public void userExists() {
        // Setup: Create test user in database
        userRepository.save(new User(12345, "user@example.com", "John", "Doe"));
    }
}
```

**Contract Tests Must Pass:**

- [x] Consumer contract tests pass (checkout-service expects correct v3 response)
- [x] Provider contract tests pass (user-service satisfies checkout-service expectations)
- [x] Contracts published to Pact Broker (https://pact-broker.example.com)
- [x] All consuming teams' contracts verified

#### **3. Backward Compatibility Verified**

- [x] v2 endpoints still functional (if maintaining backward compatibility)
- [x] v2 automated tests still passing
- [x] Manual test: Old mobile app (v2.5) against new backend
- [x] Breaking changes documented in migration guide

**Compatibility Test Script:**

```bash
#!/bin/bash
# Test backward compatibility

echo "Testing v2 endpoint (old mobile app compatibility)..."
curl -X GET "https://api-staging.example.com/v2/users/12345" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo "Expected: fullName field present"
echo "Actual: $(curl -s https://api-staging.example.com/v2/users/12345 \
  -H "Authorization: Bearer $TOKEN" | jq '.fullName')"

echo "\nTesting v3 endpoint (new apps)..."
curl -X GET "https://api-staging.example.com/v3/users/12345" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo "Expected: firstName and lastName fields present"
```

#### **4. Versioning Correctly Implemented**

- [x] API version in URL matches OpenAPI spec (`/v3/users`)
- [x] Semantic version incremented correctly (v2.5.3 → v3.0.0)
- [x] v2 endpoints include deprecation headers

**Deprecation Headers (v2 Response):**

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-API-Version: v2
X-API-Deprecated: true
X-API-Sunset-Date: 2026-07-01
X-API-Supported-Versions: v2, v3
Link: <https://docs.example.com/api/migration/v2-to-v3>; rel="deprecation"
Sunset: Wed, 01 Jul 2026 00:00:00 GMT

{
  "userId": 12345,
  "email": "user@example.com",
  "fullName": "John Doe"
}
```

#### **5. Documentation Complete**

- [x] **Changelog** updated (`CHANGELOG.md`)
- [x] **Migration guide** published (Confluence)
- [x] **API reference** generated from OpenAPI spec (Swagger UI)
- [x] **Slack announcement** posted to `#api-updates`

**Example Changelog:**

```markdown
# User Service API Changelog

## v3.0.0 - 2026-01-15 (BREAKING CHANGE)

### Breaking Changes
- ❌ Removed `fullName` field from User model
- ✅ Added `firstName` and `lastName` fields to User model

### Migration Guide

**Before (v2):**
```
{
  "userId": 12345,
  "email": "user@example.com",
  "fullName": "John Doe"
}
```

**After (v3):**
```
{
  "userId": 12345,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890"
}
```

**Client Update Required:**
```
// OLD (v2)
const userName = response.data.fullName;

// NEW (v3)
const userName = `${response.data.firstName} ${response.data.lastName}`;
```

**Timeline:**
- v3 launch: 2026-01-15
- v2 deprecation notice: 2026-01-15
- v2 sunset: 2026-07-01 (6 months)

**Support:**
- Mobile iOS v3.0: Submitted 2026-01-20, approved 2026-01-27
- Mobile Android v3.0: Submitted 2026-01-20, approved 2026-01-25
- Web: Immediately uses v3
```

#### **6. Mobile App Compatibility Tested**

- [x] Tested with production mobile app version (v2.5 - iOS/Android)
- [x] Tested with beta mobile app version (v3.0 - iOS/Android)
- [x] Confirmed old mobile apps still work (v2 endpoint)
- [x] Mobile team notified of v3 availability

**Mobile Compatibility Matrix:**

| **Platform** | **App Version** | **API Version** | **Status** | **Notes** |
|--------------|-----------------|-----------------|------------|-----------|
| iOS | v2.5 (current) | v2 | ✅ Works | Uses `/v2/users`, receives `fullName` |
| iOS | v3.0 (beta) | v3 | ✅ Works | Uses `/v3/users`, receives `firstName`/`lastName` |
| Android | v2.5 (current) | v2 | ✅ Works | Uses `/v2/users`, receives `fullName` |
| Android | v3.0 (beta) | v3 | ✅ Works | Uses `/v3/users`, receives `firstName`/`lastName` |
| Web | Latest | v3 | ✅ Works | Always uses latest API version |

#### **7. Deployment Complete**

- [x] Deployed to DEV (auto-deploy on merge)
- [x] Deployed to QA (manual promotion)
- [x] Deployed to Staging (manual promotion + stakeholder approval)
- [x] Deployed to Production (manual promotion + release checklist)
- [x] API Gateway routing configured for v3
- [x] Health check endpoint responding (GET `/v3/health`)

#### **8. Monitoring & Alerts Configured**

- [x] **Datadog dashboard** shows v3 endpoint metrics:
  - Request rate (per endpoint)
  - P50, P95, P99 latency
  - Error rate (4xx, 5xx)
  - Version usage (v2 vs v3 traffic split)

- [x] **Alerts configured:**
  - Error rate > 5% for 5 minutes → Slack `#core-team-alerts`
  - P95 latency > 500ms for 5 minutes → Slack `#core-team-alerts`
  - v2 usage increase (possible mobile app update failure) → Slack `#api-updates`

**Datadog Query Examples:**

```
# Track v2 vs v3 usage
sum:http.requests{service:user-service,api_version:v2}
sum:http.requests{service:user-service,api_version:v3}

# Alert on high error rate
avg(last_5m):sum:http.requests{service:user-service,api_version:v3,status:5xx}
  / sum:http.requests{service:user-service,api_version:v3} * 100 > 5
```

#### **9. Stakeholder Notification**

- [x] **Email sent** to api-consumers@example.com:
  - New v3 available
  - Changes summary
  - Migration guide link
  - v2 deprecation timeline

- [x] **Slack announcement** posted to `#api-updates`:

```markdown
🚀 **New API Version Released: User Service v3.0.0**

**Released:** 2026-01-15 14:30 UTC  
**Team:** Core Team

**What's New:**
• ✨ User names split into `firstName` and `lastName` for better flexibility
• ✨ Added optional `phoneNumber` field
• ⚠️ Removed deprecated `fullName` field (use `firstName` + `lastName`)

**Breaking Changes:**
⚠️ This is a MAJOR version bump (v2 → v3)

**Migration Required:**
- Web app: Already updated ✅
- Mobile iOS v3.0: Submitted to App Store (approval ~7 days)
- Mobile Android v3.0: Submitted to Play Store (approval ~3 days)

**Backward Compatibility:**
✅ v2 remains active for 6 months (sunset: July 1, 2026)
✅ Old mobile apps will continue working

**Documentation:**
📖 API Docs: https://api-docs.example.com/user-service/v3
📖 Migration Guide: https://docs.example.com/api/migration/v2-to-v3
📖 OpenAPI Spec: https://github.com/company/api-specs/blob/main/user-service-v3.yaml

**Questions?** Reply in thread or DM @core-team-lead
```

**DoD Sign-Off:** QA Lead + Release Manager approve before "Done".[36][29]

***

## Kafka & Event-Driven Communication

### Technology Stack: Apache Kafka + Kafka Streams

**Kafka Use Cases in Your Platform**:[5][40][4][6]

1. **Asynchronous Inter-Service Communication**: Decoupled event-driven messaging
2. **Event Sourcing**: Store state changes as immutable event log
3. **Real-Time Stream Processing**: Transform, aggregate, enrich data with Kafka Streams
4. **Data Integration**: Sync data across microservices without tight coupling

### Communication Patterns

**HTTP/REST APIs** (Synchronous):[3][5]
```
checkout-service → (HTTP POST) → payment-service → (HTTP response)
```
**Use for:** Request-response, real-time queries, low-latency requirements

**Apache Kafka** (Asynchronous):[4][5][6]
```
order-service → (Kafka event: OrderCreated) → [order-events topic]
  ↓
  → notification-service (sends confirmation email)
  → inventory-service (reserves stock)
  → analytics-service (tracks revenue)
```
**Use for:** Fire-and-forget, fan-out to multiple consumers, event logging, audit trails

### Kafka Topic Naming Convention

**Format:** `{domain}.{entity}.{event-type}`

```
# Retail Team topics
retail.order.created
retail.order.updated
retail.order.cancelled
retail.cart.abandoned
retail.product.viewed

# Core Team topics
core.user.registered
core.user.updated
core.payment.initiated
core.payment.completed
core.payment.failed

# AML Team topics
aml.transaction.flagged
aml.transaction.reviewed
aml.risk-assessment.completed

# Cross-team topics
system.deployment.started
system.deployment.completed
system.incident.created
```

### Kafka Schema Management (Avro)

**Why Schemas?**[40][6]
- Enforce data contracts between producers and consumers
- Prevent breaking changes (schema evolution)
- Enable backward/forward compatibility

**Example Avro Schema (`order-created-v1.avsc`):**

```json
{
  "type": "record",
  "namespace": "com.example.retail.events",
  "name": "OrderCreated",
  "version": 1,
  "fields": [
    {
      "name": "orderId",
      "type": "string",
      "doc": "Unique order identifier"
    },
    {
      "name": "userId",
      "type": "long",
      "doc": "User who placed the order"
    },
    {
      "name": "totalAmount",
      "type": "double",
      "doc": "Order total in USD"
    },
    {
      "name": "createdAt",
      "type": "long",
      "logicalType": "timestamp-millis",
      "doc": "Order creation timestamp"
    },
    {
      "name": "items",
      "type": {
        "type": "array",
        "items": {
          "type": "record",
          "name": "OrderItem",
          "fields": [
            {"name": "productId", "type": "string"},
            {"name": "quantity", "type": "int"},
            {"name": "price", "type": "double"}
          ]
        }
      }
    }
  ]
}
```

**Schema Registry:** Store schemas in Confluent Schema Registry or Apicurio Registry.

### Kafka Streams for Real-Time Processing

**Use Case: AML Team - Real-Time Transaction Monitoring**[7][6]

```java
// Kafka Streams topology for transaction monitoring
@Configuration
public class TransactionMonitoringStream {
    
    @Bean
    public KStream<String, Transaction> transactionStream(StreamsBuilder builder) {
        
        // 1. Read from transaction topic
        KStream<String, Transaction> transactions = builder
            .stream("core.payment.completed",
                Consumed.with(Serdes.String(), transactionSerde()));
        
        // 2. Filter high-value transactions (> $10,000)
        KStream<String, Transaction> highValueTx = transactions
            .filter((key, tx) -> tx.getAmount() > 10000);
        
        // 3. Aggregate by user (5-minute window)
        KTable<Windowed<String>, Double> userSpending = highValueTx
            .groupBy((key, tx) -> tx.getUserId())
            .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
            .aggregate(
                () -> 0.0,
                (userId, tx, total) -> total + tx.getAmount(),
                Materialized.with(Serdes.String(), Serdes.Double())
            );
        
        // 4. Flag suspicious activity (> $50,000 in 5 min)
        userSpending
            .toStream()
            .filter((windowedUser, totalSpent) -> totalSpent > 50000)
            .mapValues((windowedUser, totalSpent) -> 
                new RiskAlert(windowedUser.key(), totalSpent, RiskLevel.HIGH))
            .to("aml.transaction.flagged",
                Produced.with(WindowedSerdes.timeWindowedSerdeFrom(String.class),
                    riskAlertSerde()));
        
        return transactions;
    }
}
```

**Result:** AML team detects high-risk activity in real-time without polling database.

### Kafka Topic CODEOWNERS

Add to `.github/CODEOWNERS`:

```plaintext
# Kafka topics and schemas require multi-team approval
/kafka/topics/retail.* @retail-team-backend
/kafka/topics/core.* @core-team-backend
/kafka/topics/aml.* @aml-team-backend
/kafka/topics/system.* @platform-lead

# Cross-team topics (multiple consumers)
/kafka/topics/core.payment.* @core-team-backend @aml-team-backend
/kafka/topics/retail.order.* @retail-team-backend @core-team-backend

# Schema changes require schema registry admin approval
/kafka/schemas/ @platform-lead @schema-admin
```

### Kafka Event Notification in Slack

**When new Kafka topic created or schema updated:**

```markdown
📡 **Kafka Topic Created: retail.order.created**

**Created by:** Retail Team (@retail-team-lead)  
**Date:** 2026-01-15 10:30 UTC

**Topic Details:**
- **Partitions:** 6
- **Replication Factor:** 3
- **Retention:** 7 days
- **Cleanup Policy:** delete

**Schema Version:** v1  
**Schema Registry:** https://schema-registry.example.com/subjects/retail.order.created-value/versions/1

**Consumers (Expected):**
- notification-service (Redesign Team) - sends order confirmation emails
- inventory-service (Retail Team) - reserves stock
- analytics-service (AML Team) - tracks revenue metrics

**Producer:**
- order-service (Retail Team)

**Event Rate (Expected):** ~500 events/sec during peak hours

**Documentation:** https://docs.example.com/kafka/topics/retail.order.created

**Questions?** Reply in thread or contact @retail-team-lead
```

***

## Deployment Process Overview

### Current Problems & Solutions

| **Problem** | **Solution** |
|-------------|--------------|
| Manual deployments bypass QA | Automated CI/CD with mandatory quality gates |
| No release visibility | Slack notifications with metadata (branch, commit, version, team) |
| Lost deployment context | Release notes auto-generated from Jira |
| Single person knows process | Complete documentation + video walkthroughs |
| Concurrent team conflicts | Service locks + merge queue + daily standups |
| API breaking changes undetected | OpenAPI spec validation + contract tests in CI/CD |
| Kafka schema drift | Schema Registry validation + compatibility checks |

### Deployment Stages

```
Feature Branch (team-specific naming)
    ↓
Pull Request (CODEOWNERS approval required)
    ↓
CI Pipeline:
  - Build Docker image
  - Unit tests (>80% coverage)
  - OpenAPI spec validation (if API changes)
  - Kafka schema validation (if event changes)
  - Contract tests (Pact)
  - SonarQube code quality
  - Security scan (Snyk, OWASP)
    ↓
Merge to Main (after approvals + CI passes)
    ↓
CD Pipeline:
  - Auto-deploy to DEV
  - Integration tests
  - Smoke tests
    ↓
Manual Promotion to QA (Definition of Ready checklist)
    ↓
QA Testing:
  - Regression tests
  - Exploratory testing
  - Performance tests
  - Mobile compatibility tests (if API changes)
    ↓
Manual Promotion to Staging (Release Manager approval)
    ↓
Staging Validation:
  - Final smoke tests
  - Canary deployment test
  - Load testing
    ↓
Production Deployment (Release Checklist + Change Advisory Board)
    ↓
Blue-Green Deployment with Canary:
  - 10% traffic → new version (5 min monitoring)
  - 50% traffic → new version (10 min monitoring)
  - 100% traffic → new version
    ↓
Health Checks & Monitoring
    ↓
Slack Notification (team, version, API changes, Kafka topics)
    ↓
Release Notes Generated (Jira → Confluence)
```

***

## Git Branching Strategy & Feature Stand Deployment

### Overview

This section defines the git branching strategy and feature stand deployment process for managing parallel development across four cross-functional teams (Retail, Core, AML, Redesign). The strategy enables teams to work independently while maintaining code quality and enabling parallel QA and release processes.

### Branch Structure

#### Core Branches

```
main (or master)
├── Production-ready code only
├── Protected branch (requires PR + approvals)
└── Auto-deploys to Production after staging validation

dev
├── Integration branch for all teams
├── All feature branches merge here first
├── Auto-deploys to DEV environment
└── Used for daily integration testing
```

#### Feature Branches

**Naming Convention:** `feature/{TEAM-PREFIX}-{TICKET-NUMBER}-{short-description}`

**Team Prefixes:**
- **CORE**: Core Team (authentication, payments, user management)
- **RETAIL**: Retail Team (customer-facing features, checkout, catalog)
- **AML**: AML Team (anti-money laundering, compliance)
- **REDESIGN**: Redesign Team (UI modernization, frontend integration)

**Examples:**
```
feature/CORE-100-implement-oauth2
feature/RETAIL-250-add-product-search
feature/AML-75-enhance-kyc-checks
feature/REDESIGN-42-update-checkout-ui
```

**Branch Lifecycle:**
1. Created from `dev` branch
2. Developer works on feature
3. PR created to `dev` (for integration) or `main` (for direct production path)
4. After merge, branch can be deleted (or kept for feature stand deployment)

### Feature Stand Deployment Process

#### Concept

A **feature stand** is a fully functional, isolated environment created on-demand for testing cross-service features. When a developer deploys their feature branch to a feature stand, the infrastructure automatically:

1. Creates a complete environment with all microservices
2. Deploys each service from `dev` branch by default
3. **Overrides** to deploy from the feature branch if that branch exists in the service's repository
4. Provides a unique URL for QA testing

#### Workflow

```
Developer completes feature work
    ↓
Developer creates feature branch: feature/CORE-100-oauth2
    ↓
Developer pushes branch to GitLab
    ↓
Developer clicks "Deploy to Feature Stand" in GitLab UI
    ↓
Infrastructure Automation:
  - Detects feature branch name: CORE-100
  - Scans all service repositories for branch: feature/CORE-100-*
  - Creates Kubernetes namespace: feature-stand-core-100
  - For each service:
    ├── If branch exists: feature/CORE-100-* → deploy from that branch
    └── If branch doesn't exist: deploy from dev branch
  - Configures service discovery, databases, Kafka topics
  - Generates unique URL: https://feature-stand-core-100.example.com
    ↓
Feature Stand Ready
    ↓
QA Team receives notification with:
  - Feature stand URL
  - List of services deployed from feature branch
  - List of services deployed from dev branch
  - Branch names and commit SHAs
    ↓
QA performs testing on feature stand
    ↓
After QA approval → Move to Code Review
    ↓
PR created/updated → Code Review → Merge to dev/main
```

#### Branch Detection Logic

**Infrastructure Script (Pseudo-code):**

```python
def deploy_feature_stand(feature_branch_name):
    """
    Deploys a feature stand environment based on branch naming.
    
    Args:
        feature_branch_name: e.g., "CORE-100" (extracted from feature/CORE-100-oauth2)
    """
    # Extract team prefix and ticket number
    team_prefix, ticket_number = parse_branch_name(feature_branch_name)
    # e.g., "CORE", "100"
    
    # List of all microservices
    services = [
        "user-service", "auth-service", "payment-service",
        "product-service", "order-service", "checkout-service",
        "kyc-service", "compliance-service", "frontend-service"
    ]
    
    deployment_config = {}
    
    for service in services:
        repo_url = f"https://gitlab.com/company/{service}.git"
        
        # Check if feature branch exists in this service's repo
        branch_patterns = [
            f"feature/{feature_branch_name}-*",  # Exact match: feature/CORE-100-*
            f"feature/{team_prefix}-{ticket_number}-*",  # Team match: feature/CORE-100-*
        ]
        
        feature_branch = None
        for pattern in branch_patterns:
            if branch_exists(repo_url, pattern):
                feature_branch = find_matching_branch(repo_url, pattern)
                break
        
        if feature_branch:
            # Deploy from feature branch
            deployment_config[service] = {
                "branch": feature_branch,
                "source": "feature_branch",
                "reason": f"Feature branch {feature_branch} found"
            }
        else:
            # Deploy from dev branch (default)
            deployment_config[service] = {
                "branch": "dev",
                "source": "dev_branch",
                "reason": f"No feature branch found, using dev"
            }
    
    # Create Kubernetes namespace
    namespace = f"feature-stand-{team_prefix.lower()}-{ticket_number}"
    create_namespace(namespace)
    
    # Deploy each service
    for service, config in deployment_config.items():
        deploy_service(
            service=service,
            namespace=namespace,
            branch=config["branch"],
            environment="feature-stand"
        )
    
    # Configure service discovery, databases, etc.
    setup_feature_stand_infrastructure(namespace)
    
    # Generate and return feature stand URL
    feature_stand_url = f"https://{namespace}.example.com"
    return {
        "url": feature_stand_url,
        "namespace": namespace,
        "deployments": deployment_config
    }
```

### GitLab CI/CD Configuration

#### Feature Stand Deployment Job

**`.gitlab-ci.yml` (in each service repository):**

```yaml
stages:
  - build
  - test
  - deploy-dev
  - deploy-feature-stand
  - deploy-qa
  - deploy-staging
  - deploy-production

variables:
  DOCKER_REGISTRY: registry.gitlab.com/company
  KUBERNETES_NAMESPACE_DEV: microservices-dev
  KUBERNETES_NAMESPACE_QA: microservices-qa

# Standard CI pipeline for all branches
build:
  stage: build
  script:
    - mvn clean package -DskipTests
    - docker build -t $DOCKER_REGISTRY/$CI_PROJECT_NAME:$CI_COMMIT_SHA .
    - docker push $DOCKER_REGISTRY/$CI_PROJECT_NAME:$CI_COMMIT_SHA
  only:
    - branches

test:
  stage: test
  script:
    - mvn test
    - mvn jacoco:report
  coverage: '/Total.*?([0-9]{1,3})%/'
  only:
    - branches

# Auto-deploy to DEV when merged to dev branch
deploy-dev:
  stage: deploy-dev
  script:
    - kubectl set image deployment/$CI_PROJECT_NAME \
        $CI_PROJECT_NAME=$DOCKER_REGISTRY/$CI_PROJECT_NAME:$CI_COMMIT_SHA \
        -n $KUBERNETES_NAMESPACE_DEV
  only:
    - dev
  when: on_success

# Feature Stand Deployment (Manual Trigger)
deploy-feature-stand:
  stage: deploy-feature-stand
  script:
    - |
      # Extract feature branch identifier (e.g., CORE-100 from feature/CORE-100-oauth2)
      FEATURE_ID=$(echo $CI_COMMIT_REF_NAME | sed 's/feature\///' | cut -d'-' -f1-2)
      # e.g., "CORE-100"
      
      NAMESPACE="feature-stand-$(echo $FEATURE_ID | tr '[:upper:]' '[:lower:]' | tr '-' '-')"
      # e.g., "feature-stand-core-100"
      
      # Create namespace if it doesn't exist
      kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -
      
      # Deploy this service to feature stand
      kubectl set image deployment/$CI_PROJECT_NAME \
        $CI_PROJECT_NAME=$DOCKER_REGISTRY/$CI_PROJECT_NAME:$CI_COMMIT_SHA \
        -n $NAMESPACE
      
      # Update ingress with feature stand URL
      kubectl annotate ingress $CI_PROJECT_NAME \
        -n $NAMESPACE \
        feature-stand-url="https://$NAMESPACE.example.com" \
        --overwrite
      
      # Notify QA team
      curl -X POST $SLACK_WEBHOOK_URL \
        -H 'Content-Type: application/json' \
        -d "{
          \"text\": \"Feature Stand Deployed: $FEATURE_ID\",
          \"blocks\": [{
            \"type\": \"section\",
            \"text\": {
              \"type\": \"mrkdwn\",
              \"text\": \"*Feature Stand Ready*\n*Feature:* $FEATURE_ID\n*Service:* $CI_PROJECT_NAME\n*URL:* https://$NAMESPACE.example.com\n*Branch:* $CI_COMMIT_REF_NAME\n*Commit:* $CI_COMMIT_SHA\"
            }
          }]
        }"
  when: manual
  only:
    - /^feature\/.*$/
  environment:
    name: feature-stand/$CI_COMMIT_REF_NAME
    url: https://feature-stand-$(echo $CI_COMMIT_REF_NAME | sed 's/feature\///' | cut -d'-' -f1-2 | tr '[:upper:]' '[:lower:]').example.com

# QA deployment (after feature stand testing)
deploy-qa:
  stage: deploy-qa
  script:
    - kubectl set image deployment/$CI_PROJECT_NAME \
        $CI_PROJECT_NAME=$DOCKER_REGISTRY/$CI_PROJECT_NAME:$CI_COMMIT_SHA \
        -n $KUBERNETES_NAMESPACE_QA
  only:
    - dev
    - main
  when: manual
  environment:
    name: qa
    url: https://qa.example.com
```

#### Central Feature Stand Orchestration

**Infrastructure Repository: `.gitlab-ci.yml` (or Terraform/Ansible script):**

```yaml
# This job runs in a central infrastructure repository
# It orchestrates feature stand creation across all services

deploy-feature-stand-orchestration:
  stage: deploy
  script:
    - |
      # This script is triggered when a developer clicks "Deploy Feature Stand"
      # It scans all service repositories and creates the feature stand
      
      FEATURE_BRANCH_NAME=$CI_COMMIT_REF_NAME  # e.g., "feature/CORE-100-oauth2"
      FEATURE_ID=$(echo $FEATURE_BRANCH_NAME | sed 's/feature\///' | cut -d'-' -f1-2)
      # e.g., "CORE-100"
      
      SERVICES=(
        "user-service"
        "auth-service"
        "payment-service"
        "product-service"
        "order-service"
        "checkout-service"
        "kyc-service"
        "compliance-service"
        "frontend-service"
      )
      
      NAMESPACE="feature-stand-$(echo $FEATURE_ID | tr '[:upper:]' '[:lower:]')"
      
      # Create namespace
      kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -
      
      # For each service, check if feature branch exists and trigger deployment
      for SERVICE in "${SERVICES[@]}"; do
        # Check if branch exists in service repo
        if git ls-remote --heads https://gitlab.com/company/$SERVICE.git | grep -q "feature/$FEATURE_ID"; then
          echo "Found feature branch for $SERVICE, deploying from feature branch"
          # Trigger deployment job in service's CI pipeline
          curl -X POST "https://gitlab.com/api/v4/projects/company%2F$SERVICE/trigger/pipeline" \
            -F "token=$CI_JOB_TOKEN" \
            -F "ref=feature/$FEATURE_ID-*" \
            -F "variables[FEATURE_STAND_NAMESPACE]=$NAMESPACE" \
            -F "variables[DEPLOY_TO_FEATURE_STAND]=true"
        else
          echo "No feature branch for $SERVICE, deploying from dev"
          # Trigger deployment from dev branch
          curl -X POST "https://gitlab.com/api/v4/projects/company%2F$SERVICE/trigger/pipeline" \
            -F "token=$CI_JOB_TOKEN" \
            -F "ref=dev" \
            -F "variables[FEATURE_STAND_NAMESPACE]=$NAMESPACE" \
            -F "variables[DEPLOY_TO_FEATURE_STAND]=true"
        fi
      done
      
      # Wait for all deployments to complete
      # Configure service mesh, databases, etc.
      # Generate feature stand URL and notify teams
  when: manual
  only:
    - /^feature\/.*$/
```

### Parallel Development Workflow

#### Scenario: Multiple Teams Working on Related Features

**Example:** Core Team (CORE-100) and Retail Team (RETAIL-250) both modify `user-service` and `order-service`.

**Branch State:**
```
user-service repository:
  ├── dev (integration branch)
  ├── feature/CORE-100-oauth2 (Core Team)
  └── feature/RETAIL-250-product-search (Retail Team)

order-service repository:
  ├── dev
  ├── feature/CORE-100-oauth2 (Core Team)
  └── feature/RETAIL-250-product-search (Retail Team)
```

**Feature Stand Deployment:**

1. **Core Team deploys CORE-100 feature stand:**
   - `user-service`: Deployed from `feature/CORE-100-oauth2` ✅
   - `order-service`: Deployed from `feature/CORE-100-oauth2` ✅
   - All other services: Deployed from `dev` branch

2. **Retail Team deploys RETAIL-250 feature stand:**
   - `user-service`: Deployed from `feature/RETAIL-250-product-search` ✅
   - `order-service`: Deployed from `feature/RETAIL-250-product-search` ✅
   - All other services: Deployed from `dev` branch

3. **Both feature stands run in parallel:**
   - `https://feature-stand-core-100.example.com` (Core Team QA)
   - `https://feature-stand-retail-250.example.com` (Retail Team QA)

4. **No conflicts:** Each team tests independently on their isolated environment.

### Best Practices

#### Branch Management

1. **Always create feature branches from `dev`:**
   ```bash
   git checkout dev
   git pull origin dev
   git checkout -b feature/CORE-100-oauth2
   ```

2. **Keep feature branches short-lived:**
   - Merge to `dev` within 1-2 weeks
   - Delete branch after merge (unless needed for feature stand)

3. **Regularly sync with `dev`:**
   ```bash
   git checkout feature/CORE-100-oauth2
   git merge dev  # or git rebase dev
   ```

4. **Use descriptive branch names:**
   - ✅ Good: `feature/CORE-100-implement-oauth2`
   - ❌ Bad: `feature/oauth2` (missing team prefix and ticket)

#### Feature Stand Management

1. **Clean up feature stands after QA:**
   - Feature stands consume resources
   - Delete namespace after feature is merged to `dev` or `main`
   - Automate cleanup: Delete feature stands older than 7 days

2. **Document feature stand dependencies:**
   - If feature requires specific service versions, document in PR description
   - Use feature stand for integration testing, not unit testing

3. **Coordinate feature stand usage:**
   - Limit concurrent feature stands per team (e.g., max 2 per team)
   - Use feature stands for cross-service features, not single-service changes

#### Parallel Release Management

1. **Team Independence:**
   - Each team can release independently
   - Features merged to `dev` are automatically deployed to DEV environment
   - Teams coordinate releases through daily standups

2. **Release Coordination:**
   - Use `dev` branch for integration testing
   - Use feature stands for isolated feature testing
   - Coordinate production releases through Release Manager

3. **Conflict Resolution:**
   - If two teams modify the same service, merge conflicts resolved in `dev` branch
   - Use feature stands to test conflicting changes in isolation first
   - Daily standups to identify potential conflicts early

### Git Workflow Diagram

```
                    main (Production)
                       ↑
                       │ (After QA + Code Review)
                       │
                    dev (Integration)
                 ↙  ↓  ↘  ↙  ↓  ↘
        feature/   feature/   feature/   feature/
        CORE-100   RETAIL-250  AML-75    REDESIGN-42
           ↓           ↓          ↓           ↓
    [Feature Stand] [Feature Stand] [Feature Stand] [Feature Stand]
           ↓           ↓          ↓           ↓
         QA          QA         QA          QA
           ↓           ↓          ↓           ↓
      Code Review  Code Review Code Review Code Review
           ↓           ↓          ↓           ↓
        Merge to dev (parallel, independent)
```

### Summary

This git branching strategy enables:

✅ **Parallel Development**: Four teams work independently on feature branches  
✅ **Isolated Testing**: Feature stands provide complete environments for QA  
✅ **Dynamic Deployment**: Infrastructure automatically selects correct branch per service  
✅ **No Blocking**: Teams don't block each other's development or releases  
✅ **Quality Gates**: Code review and QA happen before merge to `dev` or `main`  
✅ **Traceability**: Branch names link to Jira tickets (CORE-100, RETAIL-250, etc.)

***

## Release Pipeline & Automation

### CI/CD Pipeline - Mandatory Checks

**GitHub Actions Workflow (`.github/workflows/deploy.yml`):**

```yaml
name: Deploy Microservice

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

env:
  SERVICE_NAME: user-service
  TEAM_NAME: core

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Build with Maven
        run: mvn clean package -DskipTests
      
      - name: Run unit tests
        run: mvn test
      
      - name: Check code coverage (>80%)
        run: |
          mvn jacoco:report
          mvn jacoco:check -Drule.coverage.minimum=0.80
  
  api-validation:
    runs-on: ubuntu-latest
    if: contains(github.event.head_commit.message, 'API')
    steps:
      - uses: actions/checkout@v3
      
      - name: Validate OpenAPI spec
        uses: char0n/swagger-editor-validate@v1
        with:
          definition-file: docs/openapi/${{ env.SERVICE_NAME }}-v3.yaml
      
      - name: Lint OpenAPI with Spectral
        run: |
          npm install -g @stoplight/spectral-cli
          spectral lint docs/openapi/${{ env.SERVICE_NAME }}-v3.yaml
      
      - name: Check for breaking changes
        uses: oasdiff/oasdiff-action@v0.0.15
        with:
          base: docs/openapi/${{ env.SERVICE_NAME }}-v2.yaml
          revision: docs/openapi/${{ env.SERVICE_NAME }}-v3.yaml
          fail-on-breaking: true
  
  kafka-validation:
    runs-on: ubuntu-latest
    if: contains(github.event.head_commit.message, 'Kafka')
    steps:
      - uses: actions/checkout@v3
      
      - name: Validate Avro schemas
        run: |
          for schema in kafka/schemas/*.avsc; do
            avro-tools compile schema $schema /tmp/compiled
          done
      
      - name: Check schema compatibility
        run: |
          # Check backward compatibility with Schema Registry
          curl -X POST \
            -H "Content-Type: application/vnd.schemaregistry.v1+json" \
            --data @kafka/schemas/order-created-v2.avsc \
            https://schema-registry.example.com/compatibility/subjects/retail.order.created-value/versions/latest
  
  contract-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Run Pact consumer tests
        run: mvn test -Dtest=*PactTest
      
      - name: Run Pact provider verification
        run: mvn test -Dtest=*PactVerificationTest
      
      - name: Publish contracts to Pact Broker
        run: mvn pact:publish
        env:
          PACT_BROKER_URL: https://pact-broker.example.com
          PACT_BROKER_TOKEN: ${{ secrets.PACT_BROKER_TOKEN }}
  
  security-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Run Snyk security scan
        uses: snyk/actions/maven@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
      
      - name: SonarQube scan
        run: |
          mvn sonar:sonar \
            -Dsonar.projectKey=${{ env.SERVICE_NAME }} \
            -Dsonar.host.url=https://sonarqube.example.com \
            -Dsonar.login=${{ secrets.SONAR_TOKEN }}
  
  docker-build:
    needs: [build, api-validation, kafka-validation, contract-tests, security-scan]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build Docker image
        run: |
          docker build -t ${{ env.SERVICE_NAME }}:${{ github.sha }} .
          docker tag ${{ env.SERVICE_NAME }}:${{ github.sha }} \
            ${{ env.SERVICE_NAME }}:latest
      
      - name: Scan Docker image
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: ${{ env.SERVICE_NAME }}:${{ github.sha }}
          format: 'sarif'
          output: 'trivy-results.sarif'
      
      - name: Push to registry
        run: |
          docker push ${{ env.SERVICE_NAME }}:${{ github.sha }}
          docker push ${{ env.SERVICE_NAME }}:latest
  
  deploy-dev:
    needs: docker-build
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to DEV
        run: |
          kubectl set image deployment/${{ env.SERVICE_NAME }} \
            ${{ env.SERVICE_NAME }}=${{ env.SERVICE_NAME }}:${{ github.sha }} \
            -n dev
          
          kubectl rollout status deployment/${{ env.SERVICE_NAME }} -n dev
      
      - name: Run smoke tests
        run: |
          ./scripts/smoke-tests.sh dev
      
      - name: Notify Slack
        uses: slackapi/slack-github-action@v1
        with:
          payload: |
            {
              "text": "✅ Deployed to DEV: ${{ env.SERVICE_NAME }}",
              "blocks": [
                {
                  "type": "section",
                  "text": {
                    "type": "mrkdwn",
                    "text": "*Team:* ${{ env.TEAM_NAME }}\n*Service:* ${{ env.SERVICE_NAME }}\n*Version:* ${{ github.sha }}\n*Environment:* DEV\n*Triggered by:* ${{ github.actor }}"
                  }
                }
              ]
            }
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_DEPLOYMENTS }}
```

### Slack Notifications - Enhanced with Team Info

**Deployment Started:**

```markdown
🚀 **Deployment Started: user-service**

**Team:** Core Team  
**Service:** user-service  
**Version:** v3.0.0  
**Branch:** main  
**Commit:** abc1234 - "Add firstName/lastName fields to User API"  
**Author:** @john-doe (Core Team)  
**Environment:** Production  
**Status:** In Progress  
**Started:** 2026-01-15 14:33 UTC

**API Changes:**
⚠️ BREAKING: User API v2 → v3 (firstName/lastName split)
📖 Migration Guide: https://docs.example.com/api/v2-to-v3

**Kafka Topics Modified:**
• core.user.updated (schema v2 → v3)

**Affected Teams:**
• Retail Team (checkout-service uses User API)
• Redesign Team (web UI displays user names)

**Monitoring:** https://datadog.example.com/dashboard/user-service
```

**Deployment Complete:**

```markdown
✅ **Production Deployment Successful**

**Team:** Core Team  
**Service:** user-service  
**Version:** v3.0.0  
**Branch:** main  
**Commit:** abc1234 - "Add firstName/lastName fields to User API"  
**Author:** @john-doe  
**Triggered By:** @release-manager  
**Duration:** 14:33 - 14:47 UTC (14 minutes)  
**Instances:** 8/8 healthy

**Changes:**
• ✨ Added firstName/lastName fields to User model
• ⚠️ Deprecated fullName field (removed in v4)
• 🔧 Improved authentication token caching
• 📡 Updated Kafka event schema (core.user.updated v3)

**API Version:**
• v3.0.0 deployed (breaking change)
• v2.5.3 still active (sunset: 2026-07-01)

**Canary Deployment:**
✅ 10% traffic (5 min) - 0 errors, p95 latency 180ms
✅ 50% traffic (10 min) - 0 errors, p95 latency 185ms
✅ 100% traffic switched - all healthy

**Rollback Command:**
```
kubectl rollout undo deployment/user-service -n production
```

**Post-Deployment Checklist:**
✅ Health checks passing
✅ Error rate < 0.1%
✅ Latency within SLA (p95 < 200ms)
✅ Mobile app compatibility verified (v2 still works)
✅ Monitoring dashboards updated

**Next Steps:**
• Monitor v2 vs v3 usage over next 7 days
• Mobile app v3.0 approval expected 2026-01-22
• Schedule v2 sunset reminder (2026-06-15)
```

***

## Database Migrations Strategy

### Expand-Contract Pattern with Separate Pipeline

**Phase 1: Expand (Backward Compatible)**

```sql
-- Migration: 2026011501_add_user_name_fields.sql
-- Team: Core Team
-- Date: 2026-01-15
-- Description: Add firstName/lastName columns (expand phase)
-- Risk: Low (nullable columns, no data loss)
-- Estimated Time: 3 seconds
-- Tested: Staging with 5M rows

BEGIN;

-- Add new columns (nullable for backward compatibility)
ALTER TABLE users 
  ADD COLUMN first_name VARCHAR(100),
  ADD COLUMN last_name VARCHAR(100);

-- Create indexes for new columns
CREATE INDEX idx_users_first_name ON users(first_name);
CREATE INDEX idx_users_last_name ON users(last_name);

-- Backfill data from existing full_name column
-- (Run as separate migration after deploy to avoid long transaction)
-- UPDATE users SET 
--   first_name = SPLIT_PART(full_name, ' ', 1),
--   last_name = SPLIT_PART(full_name, ' ', 2)
-- WHERE first_name IS NULL;

COMMIT;

-- Rollback script (if needed):
-- ALTER TABLE users DROP COLUMN first_name, DROP COLUMN last_name;
```

**Phase 2: Code Deployment (1 week later)**

Deploy new code that reads/writes `firstName` and `lastName`, but still maintains `fullName` for backward compatibility.

**Phase 3: Contract (Cleanup, 2 weeks after code stable)**

```sql
-- Migration: 2026012901_remove_full_name_column.sql
-- Team: Core Team
-- Date: 2026-01-29
-- Description: Remove deprecated full_name column (contract phase)
-- Risk: Medium (ensure all code uses new columns)
-- Estimated Time: 5 seconds

BEGIN;

-- Drop old column (only after confirming new columns populated)
ALTER TABLE users DROP COLUMN full_name;

-- Drop old index
DROP INDEX IF EXISTS idx_users_full_name;

COMMIT;

-- Rollback: Re-add column and backfill from first_name + last_name
-- (Document in incident runbook if needed)
```

### Migration Slack Notification

```markdown
🗄️ **Database Migration Applied**

**Migration:** 2026011501_add_user_name_fields.sql  
**Team:** Core Team  
**Service:** user-service  
**Database:** production (PostgreSQL 14)  
**Environment:** Production  
**Status:** ✅ Success  
**Duration:** 2.8 seconds  
**Timestamp:** 2026-01-15 02:15 UTC (off-peak window)

**Changes:**
• Added `first_name` column (VARCHAR(100), nullable)
• Added `last_name` column (VARCHAR(100), nullable)
• Created indexes on new columns

**Backward Compatibility:** ✅ Yes
• `full_name` column unchanged
• Application continues to work during migration

**Phase:** Expand (1 of 3)
**Next Steps:**
1. Deploy application code (2026-01-15 14:00 UTC)
2. Backfill data (2026-01-16 02:00 UTC)
3. Remove `full_name` column (2026-01-29)

**Rollback:** Available (migration reversible)
```
kubectl exec -it migration-pod -- \
  flyway undo -url=jdbc:postgresql://prod-db:5432/app
```

**Verification:**
```
-- Check new columns exist
SELECT first_name, last_name FROM users LIMIT 10;
```

**Monitoring:** https://datadog.example.com/dashboard/database-migrations
```

***

## Release Checklist & Management

### Pre-Release Planning (T-3 Days)

**Jira Release Setup:**

1. Create version: `v2026.01.15` (date-based versioning)
2. Tag all completed tickets with team labels:
   - `team:retail`
   - `team:core`
   - `team:aml`
   - `team:redesign`

3. **Multi-Team Release Coordination:**

```markdown
# Release v2026.01.15 - Multi-Team Coordination

## Teams Involved
- ✅ Core Team (2 services: user-service, payment-service)
- ✅ Retail Team (1 service: checkout-service)
- ⏸️ AML Team (no deployments this release)
- ⏸️ Redesign Team (frontend only, deployed separately)

## Services Deploying
| Service | Team | Version | API Changes | Kafka Changes | Dependencies |
|---------|------|---------|-------------|---------------|--------------|
| user-service | Core | v3.0.0 | ⚠️ BREAKING (v2→v3) | core.user.updated v3 | None |
| payment-service | Core | v2.5.1 | ✅ MINOR (new field) | core.payment.completed v2.1 | user-service v3.0.0 |
| checkout-service | Retail | v1.8.0 | ✅ PATCH (bug fix) | None | user-service v3.0.0, payment-service v2.5.1 |

## Deployment Order
1. **user-service** (no dependencies)
2. **payment-service** (depends on user-service)
3. **checkout-service** (depends on user-service + payment-service)

## Risk Assessment
**High Risk:**
- user-service v3.0.0 (breaking API change, affects mobile apps)

**Medium Risk:**
- payment-service v2.5.1 (new field, backward compatible)

**Low Risk:**
- checkout-service v1.8.0 (bug fix only)

## Rollback Plan
- user-service: Rollback to v2.5.3 (tested in staging)
- payment-service: Rollback to v2.5.0
- checkout-service: Rollback to v1.7.9

## Communication
- [x] Mobile team notified (3 weeks advance, 2025-12-25)
- [x] API consumers emailed (2026-01-01)
- [x] Slack announcement posted (`#api-updates`, 2026-01-10)
- [x] Confluence release notes published
```

### Release Day Coordination (Multi-Team)

**Timeline: 08:00 UTC Release Window**

**T-30 min (07:30 UTC):**
```markdown
📢 **Release Window Opening in 30 Minutes**

**Release:** v2026.01.15  
**Teams:** Core (2 services), Retail (1 service)  
**Start Time:** 08:00 UTC  
**Expected Duration:** 45 minutes

**Deployment Order:**
1. Core Team: user-service v3.0.0 (08:00)
2. Core Team: payment-service v2.5.1 (08:15)
3. Retail Team: checkout-service v1.8.0 (08:30)

**On-Call:**
- Core Team: @john-doe
- Retail Team: @jane-smith
- Platform: @devops-oncall

**Monitoring Dashboards:**
- https://datadog.example.com/dashboard/release-2026-01-15

**Go/No-Go Poll:**
React with ✅ if your team is ready
- Core Team: ___
- Retail Team: ___
- Platform: ___
- Release Manager: ___
```

**T-0 min (08:00 UTC) - Start Deployment:**

```markdown
🚀 **Release v2026.01.15 - STARTED**

**08:00 UTC - Deploying user-service v3.0.0 (Core Team)**

Status: In Progress...
Canary: 10% traffic → monitoring for 5 minutes

[Live status updates in thread]
```

**T+15 min (08:15 UTC) - First Service Complete:**

```markdown
✅ **user-service v3.0.0 - DEPLOYED**

Duration: 15 minutes  
Status: Healthy (8/8 pods)  
Error rate: 0.03% (within SLA)  
Latency p95: 175ms (target: <200ms)

**08:15 UTC - Deploying payment-service v2.5.1 (Core Team)**

Status: In Progress...
```

**T+45 min (08:45 UTC) - Release Complete:**

```markdown
🎉 **Release v2026.01.15 - COMPLETE**

**Duration:** 45 minutes (08:00 - 08:45 UTC)  
**Status:** ✅ All services healthy

**Deployed Services:**
| Service | Version | Team | Status | Instances |
|---------|---------|------|--------|-----------|
| user-service | v3.0.0 | Core | ✅ Healthy | 8/8 |
| payment-service | v2.5.1 | Core | ✅ Healthy | 6/6 |
| checkout-service | v1.8.0 | Retail | ✅ Healthy | 10/10 |

**Key Metrics:**
- Total error rate: 0.05% (baseline: 0.04%)
- Avg latency p95: 180ms (target: <200ms)
- No alerts triggered

**API Changes:**
- ⚠️ user-service v3 deployed (v2 still active, sunset: 2026-07-01)
- ✅ payment-service backward compatible

**Mobile Compatibility:**
- ✅ iOS v2.5 tested against production (works via v2 API)
- ✅ Android v2.5 tested against production (works via v2 API)
- 🚀 iOS v3.0 / Android v3.0 in review (expected approval: 2026-01-22)

**Post-Release Monitoring:**
Next 24 hours: Watch for:
- Error rate increase
- Latency degradation
- v2 vs v3 API usage patterns

**Release Notes:** https://docs.example.com/releases/v2026-01-15
```

***

## Incident Management Process

### Incident Severity Levels

| **Level** | **Description** | **Response Time** | **Teams Involved** | **Notification** |
|-----------|-----------------|-------------------|---------------------|------------------|
| **SEV-1** | Complete outage, payment failures, data loss, >1000 users | 5 min | All hands on deck, all teams swarm | Slack + PagerDuty + Phone |
| **SEV-2** | Major feature broken, 100-1000 users, significant degradation | 15 min | Owning team + Platform | Slack + PagerDuty |
| **SEV-3** | Single feature broken, 10-100 users, workaround exists | 1 hour | Owning team | Slack |
| **SEV-4** | Minor bug, <10 users, cosmetic issue | Next business day | Owning team | Jira ticket |

### Team-Specific On-Call Rotation

**PagerDuty Schedule:**

```
Week 1 (Jan 8-14):
  Primary: Core Team (@john-doe)
  Secondary: Retail Team (@jane-smith)
  Escalation: Platform Lead (@platform-lead)

Week 2 (Jan 15-21):
  Primary: Retail Team (@jane-smith)
  Secondary: AML Team (@aml-lead)
  Escalation: Platform Lead (@platform-lead)

Week 3 (Jan 22-28):
  Primary: AML Team (@aml-lead)
  Secondary: Core Team (@john-doe)
  Escalation: Platform Lead (@platform-lead)

Week 4 (Jan 29-Feb 4):
  Primary: Redesign Team (@redesign-dev) + Platform (shared)
  Secondary: Core Team (@john-doe)
  Escalation: Platform Lead (@platform-lead)
```

**Note:** Redesign Team (1 dev) has lighter on-call burden, paired with Platform for backup.

### Incident Response by Team

**Example: Payment Service Outage (SEV-1)**

```markdown
🚨 **INCIDENT DETECTED - SEV-1**

**Incident:** Payment failures (all transactions timing out)  
**Service:** payment-service  
**Owning Team:** Core Team  
**Severity:** SEV-1 (Critical - payments down)  
**Status:** Investigating  
**Reported:** 2026-01-16 14:32:15 UTC  
**Reporter:** monitoring-bot (Datadog alert)

**Current Impact:**
- Error rate: 98% (baseline: 0.05%)
- p99 latency: 30 seconds (baseline: 300ms)
- Affected users: ~2,000 (checkout blocked)
- Revenue impact: $15,000/hour

**Incident Commander:** @john-doe (Core Team)  
**Technical Lead:** @jane-smith (Core Team)  
**On-Call Engineer:** @john-doe (Core Team primary)  
**Communications Lead:** @platform-lead

**Teams Alerted:**
- ✅ Core Team (owns payment-service)
- ✅ Retail Team (checkout-service depends on payments)
- ✅ Platform Team (infrastructure support)
- ⏸️ AML Team (monitoring only, not affected)
- ⏸️ Redesign Team (monitoring only)

**Quick Links:**
📊 Metrics: https://datadog.example.com/dashboard/payment-service
📋 Runbook: https://docs.example.com/runbooks/payment-outage
🔍 Logs: https://logs.example.com/payment-service
⚙️ Service Map: https://docs.example.com/architecture/services

**Escalation Chain:**
1. Core Team on-call → @john-doe (active)
2. Core Team secondary → @team-member-2 (notified)
3. Platform Lead → @platform-lead (notified)
4. CTO → @cto (notify if not resolved in 30 min)

**All teams:** React ✅ when aware
```

**Investigation Timeline:**

```markdown
**[14:32:15]** Datadog alert fires (payment-service error rate > 5%)  
**[14:32:45]** PagerDuty pages Core Team on-call (@john-doe)  
**[14:33:00]** @john-doe acknowledges, creates incident channel `#incident-2026-01-16-001`  
**[14:35:00]** @john-doe checks recent deployments → payment-service v2.5.1 deployed 14:20 UTC  
**[14:36:00]** Hypothesis: Recent deployment caused issue  
**[14:38:00]** @john-doe reviews logs → seeing "Connection timeout to database" errors  
**[14:40:00]** Database check → connection pool exhausted (max 50, current 50)  
**[14:42:00]** Root cause: New code in v2.5.1 not releasing connections properly  
**[14:44:00]** Decision: **Rollback to v2.5.0** (fastest resolution)  
**[14:45:00]** Rollback initiated: `kubectl rollout undo deployment/payment-service -n production`  
**[14:48:00]** Rollback complete, error rate dropping  
**[14:50:30]** Error rate normalized to 0.08%, incident resolved  

**Total Duration:** 18 minutes (14:32 - 14:50 UTC)
```

**Resolution Post:**

```markdown
✅ **INCIDENT RESOLVED - SEV-1**

**Incident:** Payment failures  
**Duration:** 18 minutes (14:32 - 14:50 UTC)  
**Resolution:** Rolled back payment-service to v2.5.0  
**Root Cause:** Connection leak in v2.5.1 code

**Timeline:**
- 14:32:00 - Alert fired
- 14:35:00 - On-call investigating
- 14:44:00 - Rollback initiated
- 14:50:30 - Service recovered

**Impact:**
- Peak error rate: 98%
- Affected users: ~2,000
- Failed transactions: ~450
- Revenue impact: ~$4,500 (18 min downtime)

**Teams Involved:**
- Core Team (investigation + rollback)
- Retail Team (monitored checkout impact)
- Platform Team (Kubernetes rollback support)

**Follow-Up:**
- ✅ Postmortem scheduled: 2026-01-17 16:00 UTC
- ✅ Jira incident: INC-2456
- ✅ Fix planned: Connection pool management review
- ✅ Prevention: Add connection leak detection to CI/CD

**Lessons Learned:**
1. Connection pool monitoring should trigger alerts earlier
2. Load testing should include connection pool stress tests
3. Rollback procedure worked perfectly (3 min execution)

**Thanks to:**
@john-doe (IC), @jane-smith (TL), @platform-lead (Comms)
```

***

## Post-Incident Review (Postmortem)

### Blameless Postmortem Template

**Meeting:** 24 hours after incident resolution, 90 minutes

**Attendees:**
- Incident Commander (Core Team - @john-doe)
- Technical Lead (Core Team - @jane-smith)
- On-call engineer (Core Team - @john-doe)
- Retail Team representative (affected consumer)
- Platform Team representative (infrastructure)
- Product Owner (optional, if customer-facing)

**Postmortem Document:**

```markdown
# Postmortem: Payment Service Outage - 2026-01-16

## Summary
Payment-service experienced 18-minute outage due to database connection pool exhaustion introduced in v2.5.1 deployment.

**Severity:** SEV-1 (Critical)  
**Duration:** 18 minutes (14:32 - 14:50 UTC)  
**Impact:** 2,000 users, 450 failed transactions, $4,500 revenue loss  
**Team:** Core Team  
**Resolution:** Rollback to v2.5.0

---

## Timeline

| Time | Event | Actor |
|------|-------|-------|
| 14:20:00 | payment-service v2.5.1 deployed | Core Team |
| 14:30:00 | Connection pool begins filling | System |
| 14:32:15 | Error rate spike detected (98%) | Datadog |
| 14:32:45 | PagerDuty pages on-call | PagerDuty |
| 14:33:00 | Incident declared in Slack | @john-doe |
| 14:35:00 | Recent deployment identified as cause | @john-doe |
| 14:40:00 | Database connection pool exhaustion confirmed | @john-doe |
| 14:42:00 | Root cause: connection leak in new code | @jane-smith |
| 14:44:00 | Rollback decision made | @john-doe (IC) |
| 14:45:00 | Rollback initiated | @john-doe |
| 14:48:00 | Rollback complete | Kubernetes |
| 14:50:30 | Error rate normalized, incident resolved | @john-doe |

**Total Duration:** 18 minutes

---

## Root Cause

**Primary Cause:**  
Code merged in v2.5.1 introduced database connection leak. New payment retry logic opened connections but failed to close them in error paths.

**Code Snippet (Problematic):**
```
public PaymentResult processPayment(PaymentRequest request) {
    Connection conn = dataSource.getConnection(); // Opened
    try {
        return executePayment(conn, request);
    } catch (PaymentException e) {
        // Connection not closed in error path!
        throw new RuntimeException(e);
    }
}
```

**What Happened:**  
1. Payment failures triggered retry logic
2. Each retry opened new connection without closing previous
3. Within 10 minutes, all 50 connections exhausted
4. New payment requests waited indefinitely, timing out

**Contributing Factors:**
1. Code review missed unclosed connection in error path
2. Integration tests didn't simulate connection pool exhaustion
3. Load testing didn't run long enough to detect leak (only 5 min)
4. Connection pool alerts set too high (threshold: 90%, exhausted at 100%)
5. No database connection leak detection in CI/CD

---

## What Went Well

✅ **Rollback procedure worked perfectly** (3 minutes execution)  
✅ **On-call responded immediately** (within 2 minutes)  
✅ **Clear incident communication** (updates every 5 minutes)  
✅ **Team coordination excellent** (Core + Retail + Platform)  
✅ **Monitoring detected issue instantly** (alert within 30 seconds)  
✅ **Good runbooks** (database troubleshooting steps documented)  
✅ **Blue-green deployment** enabled fast rollback  

---

## What Could Improve

❌ **Connection leak not caught in code review**  
❌ **Load testing too short** (5 min vs production 24/7)  
❌ **Connection pool alerts too high** (90% threshold)  
❌ **No connection leak detection in CI/CD**  
❌ **Error path testing incomplete** (happy path only)  

---

## Action Items

### Preventive (Stop It Happening Again)

| Action | Owner | Due | Priority | Status |
|--------|-------|-----|----------|--------|
| Add connection leak detection to CI/CD (Mockito resource leak plugin) | Core Team | 2026-01-23 | P0 | 🟡 In Progress |
| Update code review checklist: "All resources closed in error paths?" | Tech Leads | 2026-01-18 | P1 | ✅ Done |
| Extend load testing duration (5 min → 30 min) | QA Team | 2026-01-30 | P1 | 🟡 In Progress |
| Add integration test: connection pool exhaustion scenario | Core Team | 2026-01-25 | P1 | 🔴 Not Started |
| Review all services for similar connection management patterns | All Teams | 2026-02-15 | P2 | 🔴 Not Started |

### Detective (Catch It Earlier)

| Action | Owner | Due | Priority | Status |
|--------|-------|-----|----------|--------|
| Lower connection pool alert threshold (90% → 75%) | Platform | 2026-01-18 | P0 | ✅ Done |
| Add connection pool growth rate monitoring | Platform | 2026-01-20 | P1 | 🟡 In Progress |
| Create Datadog dashboard: "Database Connection Health" | Platform | 2026-01-25 | P2 | 🔴 Not Started |
| Weekly connection pool usage report (automated) | Platform | 2026-02-01 | P2 | 🔴 Not Started |

### Responsive (Fix It Faster)

| Action | Owner | Due | Priority | Status |
|--------|-------|-----|----------|--------|
| Document connection pool troubleshooting in runbook | Core Team | 2026-01-18 | P2 | ✅ Done |
| Add "kill active connections" button in admin dashboard | Platform | 2026-02-15 | P3 | 🔴 Not Started |
| Practice rollback drill quarterly | All Teams | 2026-04-01 | P2 | 🔴 Not Started |

---

## Lessons Learned

1. **Connection management is critical**: Always use try-with-resources or explicit close in finally blocks
2. **Load testing duration matters**: 5 minutes isn't enough to detect slow leaks
3. **Alert thresholds need tuning**: 90% connection pool usage is too late
4. **Code review checklists work**: Need to add resource management checks
5. **Rollback is your friend**: Fast rollback (3 min) limited impact significantly
6. **Team coordination excellent**: Cross-team communication prevented confusion

---

## Metrics

**Detection Time (MTTD):** 30 seconds (alert fired 30s after threshold breach)  
**Response Time:** 2 minutes (on-call acknowledged within 2 min)  
**Resolution Time (MTTR):** 18 minutes (incident resolved)  
**Total Downtime:** 18 minutes  

**DORA Metrics Impact:**
- Deployment frequency: No impact (still deploying daily)
- Lead time for changes: No impact
- Change failure rate: 1 failed deployment / 15 total = **6.7%** (target: <15% ✅)
- Time to restore service: **18 minutes** (target: <1 hour ✅)

---

## Related Tickets

**Postmortem Actions:**
- DEV-1234: Add connection leak detection to CI/CD (P0)
- DEV-1235: Extend load testing duration (P1)
- OPS-1236: Lower connection pool alerts (P0) ✅ Done
- QA-1237: Add connection pool exhaustion test (P1)

**Incident Ticket:** INC-2456

---

## Sign-Off

**Reviewed by:** Core Team, Platform Team, Engineering Manager  
**Review Date:** 2026-01-17  
**Status:** ✅ Approved  
**Follow-Up:** 2026-02-15 (review action item completion)

---

**Document Owner:** Core Team Lead  
**Last Updated:** 2026-01-17  
**Next Review:** 2026-02-15
```

***

## Tools & Integration Recommendations

### Tool Stack Summary

| **Category** | **Tool** | **Purpose** | **Teams Using** | **Cost** |
|--------------|----------|-------------|-----------------|----------|
| **Source Control** | GitHub/GitLab | Code repositories, CODEOWNERS, merge queue | All teams | Included |
| **CI/CD** | GitHub Actions / GitLab CI | Automated testing, deployment | All teams | Included |
| **Kubernetes** | K8s + Helm | Container orchestration | All teams | Free (self-hosted) |
| **GitOps** | ArgoCD | Declarative deployments, rollback | Platform | Free (OSS) |
| **API Documentation** | Swagger UI + Redoc | OpenAPI spec rendering | All teams | Free (OSS) |
| **API Contract Testing** | Pact | Consumer-driven contracts | All teams | Free (OSS) + Pact Broker |
| **API Validation** | Spectral | OpenAPI linting | All teams (CI/CD) | Free (OSS) |
| **Messaging** | Apache Kafka | Event-driven communication | All teams | Free (self-hosted) |
| **Schema Registry** | Confluent Schema Registry | Avro schema management | Platform | Free (community) |
| **Monitoring** | Datadog or Prometheus+Grafana | Metrics, logs, traces | All teams | $500/mo or Free (OSS) |
| **Incident Management** | PagerDuty | On-call, alerting, escalation | All teams | $50-100/mo |
| **Database Migration** | Flyway | SQL migrations | All teams | Free (OSS) |
| **Communication** | Slack | Team communication, notifications | All teams | $8/user/mo |
| **Project Management** | Jira | Kanban board, release tracking | All teams | $10/user/mo |
| **Documentation** | Confluence | Runbooks, postmortems, guides | All teams | $10/user/mo |

### Team-Specific Tool Access

**Core Team (2 devs):**
- Full access: GitHub, Jira, Confluence, Datadog, PagerDuty
- Admin access: user-service, auth-service, payment-service repos
- Kafka admin: core.* topics

**Retail Team (3 devs):**
- Full access: GitHub, Jira, Confluence, Datadog, PagerDuty
- Admin access: product-catalog-service, cart-service, checkout-service, order-service repos
- Kafka admin: retail.* topics

**AML Team (2 devs):**
- Full access: GitHub, Jira, Confluence, Datadog, PagerDuty
- Admin access: transaction-monitor-service, compliance-report-service repos
- Kafka admin: aml.* topics
- Read access: core.payment.* topics (for monitoring)

**Redesign Team (1 dev):**
- Full access: GitHub, Jira, Confluence, Datadog
- Limited PagerDuty: Paired with Platform for on-call (lighter rotation)
- Admin access: api-gateway, web-frontend repos
- Read-only: All service repos (for API integration)

***

## Secure Git Data Practices

### Critical Rules: Never Commit Sensitive Data

**What NEVER Goes in Git:**

| **Type** | **Examples** | **Risk Level** |
|----------|-------------|----------------|
| **API Keys** | Stripe API keys, AWS access keys, Google Maps API keys | 🔴 Critical |
| **Credentials** | Database passwords, service account passwords, OAuth secrets | 🔴 Critical |
| **Private Keys** | SSH private keys, SSL/TLS certificates, signing keys | 🔴 Critical |
| **Tokens** | JWT secrets, OAuth tokens, access tokens, refresh tokens | 🔴 Critical |
| **Connection Strings** | Database connection strings with passwords, Redis URLs with auth | 🔴 Critical |
| **Environment-Specific Config** | Production database URLs, production API endpoints | 🟡 High |
| **Secrets Files** | `.env` files, `secrets.yaml`, `credentials.json` | 🔴 Critical |
| **Hardcoded Credentials** | Any passwords, keys, or tokens in source code | 🔴 Critical |

**Why This Matters:**

- **Git History is Permanent**: Even if you delete a file, it remains in git history forever
- **Public Repos**: Accidental public exposure exposes all historical commits
- **Compliance**: GDPR, PCI-DSS, SOC 2 require strict credential management
- **Security Breaches**: Leaked credentials can lead to data breaches, financial loss, reputation damage

### Backend Best Practices (Java Microservices)

#### 1. Use Environment Variables

**Never do this:**
```java
// ❌ BAD: Hardcoded credentials
public class PaymentService {
    private static final String STRIPE_API_KEY = "sk_live_51AbCdEfGhIjKlMnOpQrStUvWxYz";
    private static final String DB_PASSWORD = "mySecretPassword123";
}
```

**Do this instead:**
```java
// ✅ GOOD: Environment variables
@Configuration
public class PaymentConfig {
    @Value("${stripe.api.key}")
    private String stripeApiKey;
    
    @Value("${database.password}")
    private String dbPassword;
}
```

**Application Properties (`application.yml`):**
```yaml
# ✅ GOOD: Reference environment variables
stripe:
  api:
    key: ${STRIPE_API_KEY}

database:
  password: ${DB_PASSWORD}
  url: ${DB_URL:jdbc:postgresql://localhost:5432/mydb}
```

#### 2. Use Spring Cloud Config or Kubernetes Secrets

**Spring Cloud Config Server (`application.yml`):**
```yaml
# config-server/application.yml
spring:
  cloud:
    config:
      server:
        git:
          uri: ${CONFIG_REPO_URL}
          search-paths: '{application}'
          default-label: main
```

**Kubernetes Secret (for production):**
```yaml
# k8s/secrets/payment-service-secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: payment-service-secrets
  namespace: production
type: Opaque
stringData:
  stripe-api-key: "sk_live_..."
  db-password: "secure-password"
```

**Deployment references secret:**
```yaml
# k8s/deployments/payment-service.yaml
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
      - name: payment-service
        env:
        - name: STRIPE_API_KEY
          valueFrom:
            secretKeyRef:
              name: payment-service-secrets
              key: stripe-api-key
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: payment-service-secrets
              key: db-password
```

#### 3. Use `.gitignore` for Local Development

**`.gitignore` (Java/Spring Boot):**
```gitignore
# Environment files
.env
.env.local
.env.*.local
*.env

# Configuration with secrets
application-local.yml
application-prod.yml
application-secrets.yml
config/local.properties
config/secrets.properties

# IDE files (may contain paths)
.idea/
.vscode/
*.iml

# Logs (may contain sensitive data)
logs/
*.log

# Build artifacts
target/
build/
*.class
*.jar
*.war

# Temporary files
*.tmp
*.swp
*.bak
```

#### 4. Use `.env.example` Template

**Create `.env.example` (committed to git):**
```bash
# .env.example
# Copy this file to .env and fill in your values
# DO NOT commit .env to git

# Stripe API Keys
STRIPE_API_KEY=sk_test_your_key_here
STRIPE_PUBLISHABLE_KEY=pk_test_your_key_here

# Database
DB_URL=jdbc:postgresql://localhost:5432/mydb
DB_USERNAME=your_username
DB_PASSWORD=your_password

# JWT Secrets
JWT_SECRET=your_jwt_secret_here
JWT_EXPIRATION=86400000

# External Services
AWS_ACCESS_KEY_ID=your_aws_key
AWS_SECRET_ACCESS_KEY=your_aws_secret
AWS_REGION=us-east-1
```

**README.md instructions:**
```markdown
## Local Development Setup

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Fill in your local credentials in `.env`

3. Never commit `.env` to git (already in `.gitignore`)
```

#### 5. Use Secrets Management Tools

**For Production:**
- **AWS Secrets Manager**: Store secrets in AWS, rotate automatically
- **HashiCorp Vault**: Centralized secrets management
- **Azure Key Vault**: Microsoft Azure secrets storage
- **Google Secret Manager**: GCP secrets management

**Example with AWS Secrets Manager:**
```java
// ✅ GOOD: Fetch from AWS Secrets Manager
@Service
public class SecretsService {
    @Autowired
    private AWSSecretsManager secretsManager;
    
    public String getStripeApiKey() {
        GetSecretValueRequest request = new GetSecretValueRequest()
            .withSecretId("payment-service/stripe-api-key");
        GetSecretValueResult result = secretsManager.getSecretValue(request);
        return result.getSecretString();
    }
}
```

### Frontend Best Practices (Web Applications)

#### 1. Never Commit API Keys in Frontend Code

**Never do this:**
```javascript
// ❌ BAD: Hardcoded API key in frontend
const GOOGLE_MAPS_API_KEY = "AIzaSyAbCdEfGhIjKlMnOpQrStUvWxYz123456";
const STRIPE_PUBLISHABLE_KEY = "pk_live_51AbCdEfGhIjKlMnOpQrStUvWxYz";
```

**Do this instead:**
```javascript
// ✅ GOOD: Environment variables (build-time)
const GOOGLE_MAPS_API_KEY = process.env.REACT_APP_GOOGLE_MAPS_API_KEY;
const STRIPE_PUBLISHABLE_KEY = process.env.REACT_APP_STRIPE_PUBLISHABLE_KEY;
```

**Note:** Frontend environment variables are embedded in the build, so only use **public** keys (like Stripe publishable keys). Never use **secret** keys in frontend code.

#### 2. Use Environment Variables (Build-Time)

**`.env.local` (not committed):**
```bash
# .env.local
REACT_APP_GOOGLE_MAPS_API_KEY=AIzaSy...
REACT_APP_STRIPE_PUBLISHABLE_KEY=pk_test_...
REACT_APP_API_BASE_URL=https://api.example.com
```

**`.env.example` (committed):**
```bash
# .env.example
REACT_APP_GOOGLE_MAPS_API_KEY=your_google_maps_key_here
REACT_APP_STRIPE_PUBLISHABLE_KEY=your_stripe_publishable_key_here
REACT_APP_API_BASE_URL=https://api-staging.example.com
```

**`.gitignore`:**
```gitignore
# Environment files
.env
.env.local
.env.*.local
.env.production.local
.env.development.local
```

#### 3. Use Backend Proxy for Secret Keys

**For secret operations, proxy through backend:**
```javascript
// ✅ GOOD: Secret operations go through backend
// Frontend calls backend API, backend uses secret key
async function processPayment(paymentData) {
  const response = await fetch('/api/payments/process', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(paymentData)
  });
  return response.json();
}

// Backend (Java) uses secret Stripe key:
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Value("${stripe.secret.key}")  // From environment variable
    private String stripeSecretKey;
    
    @PostMapping("/process")
    public PaymentResult processPayment(@RequestBody PaymentRequest request) {
        // Use secret key server-side only
        Stripe.apiKey = stripeSecretKey;
        // Process payment...
    }
}
```

#### 4. Use Runtime Configuration for Different Environments

**For React apps, use `public/config.js`:**
```javascript
// public/config.js (loaded at runtime)
window.APP_CONFIG = {
  apiBaseUrl: 'https://api.example.com',
  googleMapsApiKey: 'AIzaSy...',
  stripePublishableKey: 'pk_test_...'
};
```

**Load in `index.html`:**
```html
<!-- index.html -->
<script src="%PUBLIC_URL%/config.js"></script>
<script>
  // Override with environment-specific config
  if (process.env.REACT_APP_ENV === 'production') {
    window.APP_CONFIG = {
      apiBaseUrl: 'https://api.example.com',
      // ... production config
    };
  }
</script>
```

### Mobile App Best Practices (Flutter)

#### 1. Use Environment Variables with `flutter_dotenv`

**Never do this:**
```dart
// ❌ BAD: Hardcoded API key
class ApiConfig {
  static const String apiKey = "sk_live_51AbCdEfGhIjKlMnOpQrStUvWxYz";
  static const String apiBaseUrl = "https://api.example.com";
}
```

**Do this instead:**
```dart
// ✅ GOOD: Environment variables using flutter_dotenv
import 'package:flutter_dotenv/flutter_dotenv.dart';

class ApiConfig {
  static String get apiKey => dotenv.env['API_KEY'] ?? '';
  static String get apiBaseUrl => dotenv.env['API_BASE_URL'] ?? '';
  static String get stripePublishableKey => dotenv.env['STRIPE_PUBLISHABLE_KEY'] ?? '';
}

// Initialize in main.dart
Future<void> main() async {
  await dotenv.load(fileName: ".env");
  runApp(MyApp());
}
```

**Add dependency to `pubspec.yaml`:**
```yaml
dependencies:
  flutter_dotenv: ^5.1.0
```

**`.env` (not committed, in `.gitignore`):**
```bash
# .env
API_KEY=your_api_key_here
API_BASE_URL=https://api.example.com
STRIPE_PUBLISHABLE_KEY=pk_test_your_key_here
GOOGLE_MAPS_API_KEY=AIzaSy_your_key_here
```

**`.env.example` (committed):**
```bash
# .env.example
# Copy this file to .env and fill in your values
# DO NOT commit .env to git

API_KEY=your_api_key_here
API_BASE_URL=https://api-staging.example.com
STRIPE_PUBLISHABLE_KEY=pk_test_your_key_here
GOOGLE_MAPS_API_KEY=AIzaSy_your_key_here
```

**Update `pubspec.yaml` to include `.env` file:**
```yaml
flutter:
  assets:
    - .env
```

**`.gitignore`:**
```gitignore
# Flutter
.env
.env.local
.env.*.local
*.env

# Build files
build/
.dart_tool/
.packages
.pub-cache/
.pub/
```

#### 2. Use Build Configuration Files for Different Environments

**Create environment-specific config files:**

**`lib/config/app_config.dart`:**
```dart
// lib/config/app_config.dart
class AppConfig {
  static const String environment = String.fromEnvironment(
    'ENVIRONMENT',
    defaultValue: 'dev',
  );
  
  static String get apiBaseUrl {
    switch (environment) {
      case 'prod':
        return dotenv.env['API_BASE_URL_PROD'] ?? '';
      case 'staging':
        return dotenv.env['API_BASE_URL_STAGING'] ?? '';
      default:
        return dotenv.env['API_BASE_URL_DEV'] ?? 'http://localhost:8080';
    }
  }
  
  static bool get isProduction => environment == 'prod';
  static bool get isStaging => environment == 'staging';
  static bool get isDevelopment => environment == 'dev';
}
```

**Build commands for different environments:**
```bash
# Development
flutter run --dart-define=ENVIRONMENT=dev

# Staging
flutter build apk --dart-define=ENVIRONMENT=staging
flutter build ios --dart-define=ENVIRONMENT=staging

# Production
flutter build apk --release --dart-define=ENVIRONMENT=prod
flutter build ios --release --dart-define=ENVIRONMENT=prod
```

#### 3. Use `--dart-define` for CI/CD Secrets

**For CI/CD pipelines, pass secrets as build arguments:**

**GitHub Actions example:**
```yaml
# .github/workflows/build-flutter.yml
name: Build Flutter App

on:
  push:
    branches: [main]

jobs:
  build-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Flutter
        uses: subosito/flutter-action@v2
        with:
          flutter-version: '3.16.0'
      
      - name: Build APK
        run: |
          flutter build apk --release \
            --dart-define=ENVIRONMENT=prod \
            --dart-define=API_KEY=${{ secrets.API_KEY }} \
            --dart-define=API_BASE_URL=${{ secrets.API_BASE_URL }}
        env:
          API_KEY: ${{ secrets.API_KEY }}
          API_BASE_URL: ${{ secrets.API_BASE_URL }}
  
  build-ios:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Flutter
        uses: subosito/flutter-action@v2
        with:
          flutter-version: '3.16.0'
      
      - name: Build iOS
        run: |
          flutter build ios --release \
            --dart-define=ENVIRONMENT=prod \
            --dart-define=API_KEY=${{ secrets.API_KEY }} \
            --dart-define=API_BASE_URL=${{ secrets.API_BASE_URL }}
```

**Access in code:**
```dart
// lib/config/secrets.dart
class Secrets {
  static String get apiKey => const String.fromEnvironment(
    'API_KEY',
    defaultValue: '',
  );
  
  static String get apiBaseUrl => const String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'https://api.example.com',
  );
}
```

#### 4. Use `flutter_config` Package for Native Platform Integration

**For accessing secrets in native iOS/Android code:**

**Add dependency:**
```yaml
dependencies:
  flutter_config: ^2.0.0
```

**Create `flutter_config.dart`:**
```dart
import 'package:flutter_config/flutter_config.dart';

class AppSecrets {
  static String get apiKey => FlutterConfig.get('API_KEY');
  static String get apiBaseUrl => FlutterConfig.get('API_BASE_URL');
}
```

**Create `.env` file:**
```bash
API_KEY=your_api_key_here
API_BASE_URL=https://api.example.com
```

**Access in native Android (`MainActivity.kt`):**
```kotlin
import com.pauldemarco.flutter_config.FlutterConfig

class MainActivity: FlutterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apiKey = FlutterConfig.env()["API_KEY"]
        // Use apiKey in native code
    }
}
```

**Access in native iOS (`AppDelegate.swift`):**
```swift
import FlutterConfig

@UIApplicationMain
class AppDelegate: FlutterAppDelegate {
    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        let apiKey = FlutterConfig.env()["API_KEY"]
        // Use apiKey in native code
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }
}
```

#### 5. Use Environment-Specific App Icons and Names

**Create flavor-specific configurations:**

**`android/app/build.gradle`:**
```gradle
android {
    flavorDimensions "environment"
    productFlavors {
        dev {
            dimension "environment"
            applicationIdSuffix ".dev"
            resValue "string", "app_name", "MyApp Dev"
        }
        staging {
            dimension "environment"
            applicationIdSuffix ".staging"
            resValue "string", "app_name", "MyApp Staging"
        }
        prod {
            dimension "environment"
            resValue "string", "app_name", "MyApp"
        }
    }
}
```

**Build with flavor:**
```bash
# Development
flutter build apk --flavor dev

# Staging
flutter build apk --flavor staging

# Production
flutter build apk --flavor prod --release
```

### Making It Convenient for Local Development

#### 1. Create Setup Scripts

**`scripts/setup-local-dev.sh`:**
```bash
#!/bin/bash
# Setup script for local development

echo "🚀 Setting up local development environment..."

# Backend services
echo "📦 Setting up backend services..."
for service in user-service payment-service checkout-service; do
    if [ ! -f "services/$service/.env" ]; then
        echo "Creating .env for $service..."
        cp "services/$service/.env.example" "services/$service/.env"
        echo "⚠️  Please fill in credentials in services/$service/.env"
    fi
done

# Frontend
echo "🌐 Setting up frontend..."
if [ ! -f "web-frontend/.env.local" ]; then
    cp "web-frontend/.env.example" "web-frontend/.env.local"
    echo "⚠️  Please fill in API keys in web-frontend/.env.local"
fi

# Mobile Flutter
echo "📱 Setting up Flutter..."
if [ ! -f "mobile-flutter/.env" ]; then
    cp "mobile-flutter/.env.example" "mobile-flutter/.env"
    echo "⚠️  Please fill in API keys in mobile-flutter/.env"
fi

echo "✅ Setup complete! Please fill in credentials in the generated files."
```

#### 2. Use Docker Compose with Environment Files

**`docker-compose.local.yml`:**
```yaml
version: '3.8'
services:
  user-service:
    build: ./services/user-service
    env_file:
      - ./services/user-service/.env.local
    environment:
      - DB_URL=${DB_URL:-jdbc:postgresql://db:5432/userdb}
      - DB_USERNAME=${DB_USERNAME:-postgres}
      - DB_PASSWORD=${DB_PASSWORD:-postgres}
  
  payment-service:
    build: ./services/payment-service
    env_file:
      - ./services/payment-service/.env.local
    depends_on:
      - user-service
  
  db:
    image: postgres:14
    environment:
      POSTGRES_PASSWORD: ${DB_PASSWORD:-postgres}
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

**Run with:**
```bash
docker-compose -f docker-compose.local.yml up
```

#### 3. Use CI/CD Secrets for Different Stages

**GitHub Actions Secrets:**
```yaml
# .github/workflows/deploy.yml
name: Deploy to Environment

on:
  workflow_dispatch:
    inputs:
      environment:
        description: 'Environment to deploy to'
        required: true
        type: choice
        options:
          - dev
          - staging
          - production

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Deploy to ${{ github.event.inputs.environment }}
        env:
          STRIPE_API_KEY: ${{ secrets[format('STRIPE_API_KEY_{0}', github.event.inputs.environment)] }}
          DB_PASSWORD: ${{ secrets[format('DB_PASSWORD_{0}', github.event.inputs.environment)] }}
        run: |
          # Deployment script
          echo "Deploying with environment-specific secrets..."
```

**Secrets in GitHub:**
- `STRIPE_API_KEY_DEV` → Development Stripe test key
- `STRIPE_API_KEY_STAGING` → Staging Stripe test key
- `STRIPE_API_KEY_PRODUCTION` → Production Stripe live key

### Pre-Commit Hooks: Prevent Accidental Commits

#### 1. Install `git-secrets` (AWS Tool)

```bash
# Install git-secrets
git clone https://github.com/awslabs/git-secrets.git
cd git-secrets
sudo make install

# Configure for your repository
cd /path/to/your/repo
git secrets --install
git secrets --register-aws

# Add custom patterns
git secrets --add 'sk_live_[A-Za-z0-9]{32,}'
git secrets --add 'pk_live_[A-Za-z0-9]{32,}'
git secrets --add 'AIza[0-9A-Za-z_-]{35}'
```

**Test:**
```bash
# This should fail
echo "STRIPE_API_KEY=sk_live_51AbCdEfGhIjKlMnOpQrStUvWxYz" > test.txt
git add test.txt
# Error: Potential secrets detected!
```

#### 2. Use `detect-secrets` (Yelp Tool)

```bash
# Install
pip install detect-secrets

# Scan repository
detect-secrets scan --baseline .secrets.baseline

# Add to pre-commit hook
detect-secrets-hook --baseline .secrets.baseline
```

**`.pre-commit-config.yaml`:**
```yaml
repos:
  - repo: https://github.com/Yelp/detect-secrets
    rev: v1.4.0
    hooks:
      - id: detect-secrets
        args: ['--baseline', '.secrets.baseline']
```

#### 3. Use `truffleHog` for Historical Scanning

```bash
# Install
pip install trufflehog

# Scan git history
trufflehog git file://. --json
```

### If Secrets Are Already Committed: Emergency Response

#### 1. Immediate Actions

```bash
# 1. Rotate ALL exposed credentials immediately
# - Change API keys in service dashboards
# - Change database passwords
# - Revoke OAuth tokens
# - Regenerate SSH keys

# 2. Remove from git history (if private repo)
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch path/to/secret-file" \
  --prune-empty --tag-name-filter cat -- --all

# 3. Force push (coordinate with team!)
git push origin --force --all
git push origin --force --tags

# 4. Notify security team
```

#### 2. Use `BFG Repo-Cleaner` (Faster Alternative)

```bash
# Install BFG
brew install bfg  # macOS
# or download from https://rtyley.github.io/bfg-repo-cleaner/

# Remove secrets file from history
bfg --delete-files secret-file.txt

# Remove secrets from all files
bfg --replace-text passwords.txt  # File with old:new mappings

# Clean up
git reflog expire --expire=now --all
git gc --prune=now --aggressive
```

**⚠️ Warning:** Rewriting git history is destructive. Coordinate with your team and ensure everyone pulls the cleaned history.

### Checklist: Secure Git Practices

**Before Every Commit:**
- [ ] No API keys, passwords, or tokens in code
- [ ] No `.env` files committed
- [ ] No hardcoded credentials
- [ ] All secrets use environment variables
- [ ] `.gitignore` includes all secret files
- [ ] Pre-commit hooks installed and passing

**For New Developers:**
- [ ] Read this section on secure git practices
- [ ] Copy `.env.example` to `.env` (don't commit `.env`)
- [ ] Set up local development environment
- [ ] Test that app works with environment variables
- [ ] Understand secrets management for your platform (backend/frontend/mobile)

**For Production Deployments:**
- [ ] All secrets stored in secure vault (AWS Secrets Manager, Vault, etc.)
- [ ] Kubernetes secrets created (not in git)
- [ ] CI/CD uses GitHub/GitLab secrets (not hardcoded)
- [ ] Secrets rotated regularly (quarterly)
- [ ] Access to secrets audited and logged

### Tools Summary

| **Tool** | **Purpose** | **Platform** |
|----------|-------------|--------------|
| **git-secrets** | Pre-commit hook to detect secrets | All |
| **detect-secrets** | Scan for secrets in code | All |
| **truffleHog** | Scan git history for secrets | All |
| **BFG Repo-Cleaner** | Remove secrets from git history | All |
| **AWS Secrets Manager** | Store secrets in AWS | Backend (AWS) |
| **HashiCorp Vault** | Centralized secrets management | Backend |
| **Kubernetes Secrets** | Store secrets in K8s | Backend (K8s) |
| **GitHub Secrets** | Store secrets for CI/CD | CI/CD |

***

## Knowledge Documentation Standards

### Confluence Space Structure

```
📁 Platform Engineering
├── 📄 DevOps Deployment & Updates Guide (this document)
├── 📁 Team Guides
│   ├── 📄 Core Team Onboarding
│   ├── 📄 Retail Team Onboarding
│   ├── 📄 AML Team Onboarding
│   └── 📄 Redesign Team Onboarding
├── 📁 API Documentation
│   ├── 📄 API Versioning Policy
│   ├── 📄 OpenAPI Standards
│   ├── 📄 API Contract Testing Guide
│   └── 📁 Migration Guides
│       ├── 📄 User API v2 → v3 Migration
│       └── 📄 Payment API v2.5 → v3.0 Migration
├── 📁 Kafka Documentation
│   ├── 📄 Kafka Topic Naming Convention
│   ├── 📄 Schema Evolution Guide
│   ├── 📄 Kafka Streams Best Practices
│   └── 📁 Topic Registry
│       ├── 📄 core.* topics (Core Team)
│       ├── 📄 retail.* topics (Retail Team)
│       └── 📄 aml.* topics (AML Team)
├── 📁 Runbooks
│   ├── 📄 Deployment Rollback Procedure
│   ├── 📄 Database Connection Pool Issues
│   ├── 📄 Kafka Consumer Lag
│   ├── 📄 API Gateway 5xx Errors
│   └── 📄 Emergency Procedures
├── 📁 Architecture Decisions
│   ├── 📄 Why Kanban (No Sprints)
│   ├── 📄 API Versioning Strategy
│   ├── 📄 Kafka vs HTTP Communication
│   └── 📄 Database Migration Pattern (Expand-Contract)
├── 📁 Postmortems
│   ├── 📄 2026-01-16 Payment Service Outage (Core Team)
│   ├── 📄 2025-12-09 API Timeout Incident (Retail Team)
│   └── 📄 [Archived postmortems...]
└── 📁 On-Call Resources
    ├── 📄 Escalation Contacts
    ├── 📄 On-Call Schedule (PagerDuty)
    └── 📄 Handoff Checklist
```

### Onboarding Checklist (Team-Specific)

**Core Team New Developer:**

```markdown
## Week 1: Foundation

**Day 1-2: Access & Setup**
- [x] GitHub access (user-service, auth-service, payment-service repos)
- [x] Jira access (Core Team board)
- [x] Slack channels (#core-team, #deployments, #api-updates, #kanban-sync)
- [x] PagerDuty on-call schedule (added as secondary)
- [x] Datadog dashboard access
- [x] Confluence access (read all Core Team docs)
- [x] Local development environment setup

**Day 3-5: Domain Knowledge**
- [x] Read: DevOps Deployment Guide (this document)
- [x] Read: API Versioning Policy
- [x] Read: Core Team service architecture diagram
- [x] Review: Last 5 postmortems
- [x] Attend: Daily Kanban standup (observe)
- [x] Shadow: Senior developer on code review

## Week 2: Hands-On Practice

**Day 6-8: First Contribution**
- [x] Pick Jira ticket: Simple bug fix (assign yourself)
- [x] Create feature branch: `feature/CORE-XXX-description`
- [x] Write OpenAPI spec (if API change)
- [x] Implement fix + unit tests
- [x] Submit PR (request review from 2 senior devs)
- [x] Address review comments
- [x] Merge to main (after approval)

**Day 9-10: Deployment**
- [x] Watch deployment to DEV (auto-deploy after merge)
- [x] Verify deployment in DEV environment
- [x] Run smoke tests manually
- [x] Participate in daily standup (present your work)

## Week 3: Independence

**Day 11-15: Larger Task**
- [x] Pick larger Jira ticket (standard complexity)
- [x] Design API changes (if needed) + OpenAPI spec
- [x] Implement + write contract tests (Pact)
- [x] Deploy to QA (manual promotion)
- [x] Coordinate with Retail Team (if API consumed by them)
- [x] Lead deployment to Production (supervised)

## Week 4: On-Call Readiness

**Day 16-20: Incident Response**
- [x] Read: Incident Management runbooks
- [x] Participate: Mock incident drill (organized)
- [x] Shadow: On-call engineer during real incident (if occurs)
- [x] Practice: Rollback procedure in staging
- [x] Added to on-call rotation (secondary, starting Week 5)

## Sign-Off

**Completed:** ___________  
**Signed by:**  
- New Developer: ___________
- Core Team Lead: ___________
- Platform Lead: ___________
```

***

## Implementation Roadmap

### Phase 1: Foundation (Weeks 1-3)

**Week 1:**
- [x] Create Confluence space with this guide
- [x] Set up Slack channels:
  - `#kanban-sync` (daily standups)
  - `#deployments` (release notifications)
  - `#api-updates` (API version announcements)
  - `#incidents` (active incidents)
  - `#migrations` (database changes)
- [x] Configure PagerDuty with team-specific on-call rotations
- [x] Document team ownership in CODEOWNERS file

**Week 2:**
- [x] Create Jira Kanban board with swimlanes (Retail, Core, AML, Redesign)
- [x] Set WIP limits per team
- [x] Create Jira workflow with DoR/DoD gates
- [x] Set up release notes auto-generation

**Week 3:**
- [x] Document OpenAPI standards and templates
- [x] Create API versioning policy document
- [x] Set up Swagger UI for API documentation
- [x] Create first runbook (deployment rollback)

### Phase 2: API Governance & Automation (Weeks 4-7)

**Week 4:**
- [x] Implement OpenAPI validation in CI/CD (Spectral)
- [x] Add breaking change detection (oasdiff)
- [x] Create API contract testing framework (Pact)
- [x] Set up Pact Broker

**Week 5:**
- [x] Implement Slack notifications for deployments (GitHub Actions webhook)
- [x] Add team, API version, Kafka topic info to notifications
- [x] Create deployment audit table in Postgres

**Week 6:**
- [x] Set up Kafka Schema Registry
- [x] Document Kafka topic naming conventions
- [x] Create Kafka topic CODEOWNERS rules
- [x] Implement schema validation in CI/CD

**Week 7:**
- [x] Test end-to-end: API change → OpenAPI validation → contract tests → deployment
- [x] Conduct first API version migration drill
- [x] Gather feedback from all teams

### Phase 3: Incident Management (Weeks 8-10)

**Week 8:**
- [x] Document incident severity levels
- [x] Create incident response runbooks
- [x] Set up PagerDuty escalation policies
- [x] Configure Slack incident channel creation

**Week 9:**
- [x] Create postmortem template
- [x] Train all teams on incident procedures
- [x] Set up Datadog alerts with team-specific routing

**Week 10:**
- [x] Conduct incident drill (simulate SEV-2)
- [x] Review and improve incident procedures
- [x] Schedule quarterly incident drills

### Phase 4: Database Migrations (Weeks 11-14)

**Week 11:**
- [x] Document Flyway configuration
- [x] Create migration PR template
- [x] Document expand-contract pattern with examples

**Week 12:**
- [x] Create separate CI/CD pipeline for migrations
- [x] Test migration dry-run on staging
- [x] Implement migration Slack notifications

**Week 13:**
- [x] Audit existing migrations for forward-only compliance
- [x] Re-evaluate last 10 production migrations
- [x] Document findings and improvements

**Week 14:**
- [x] Train all teams on migration best practices
- [x] Create migration runbooks
- [x] Finalize and approve process

### Phase 5: Continuous Improvement (Ongoing)

**Monthly:**
- Review Kanban metrics (cycle time, throughput, WIP)
- Analyze deployment trends (frequency, success rate, duration)
- Review incident patterns (teams, services, root causes)
- Update runbooks based on lessons learned

**Quarterly:**
- Knowledge documentation audit
- Incident drill (all teams participate)
- Review API deprecation timeline (sunset old versions)
- Team retrospective (what's working, what's not)
- DORA metrics review

**Annually:**
- Full process review (DevOps guide update)
- Tool evaluation (better alternatives?)
- Team training refresh (new hires, new processes)
- Architecture review (scaling challenges?)

***

## Quick Reference

### Critical Commands

**Check deployment status:**
```bash
kubectl rollout status deployment/service-name -n production
kubectl get pods -n production -l app=service-name
```

**Rollback immediately:**
```bash
kubectl rollout undo deployment/service-name -n production
kubectl rollout undo deployment/service-name -n production --to-revision=5
```

**Scale service under load:**
```bash
kubectl scale deployment service-name --replicas=15 -n production
```

**View logs (incident investigation):**
```bash
kubectl logs -f deployment/service-name -n production --tail=200
```

**Database queries:**
```sql
-- Active connections
SELECT * FROM pg_stat_activity;

-- Slow queries
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 20;

-- Connection pool status
SELECT count(*) FROM pg_stat_activity;
```

**Kafka commands:**
```bash
# List topics
kafka-topics --list --bootstrap-server kafka:9092

# Consumer lag
kafka-consumer-groups --describe --group group-name --bootstrap-server kafka:9092

# Publish test event
kafka-console-producer --topic retail.order.created --bootstrap-server kafka:9092
```

### Escalation Contacts

**Team Leads:**
- Core Team: @core-team-lead
- Retail Team: @retail-team-lead
- AML Team: @aml-team-lead
- Redesign Team: @redesign-team-lead
- Platform: @platform-lead

**Escalation Path (SEV-1):**
1. Team on-call engineer (0-5 min)
2. Team lead (5-15 min)
3. Platform lead (15-30 min)
4. CTO (30+ min or customer-facing)

**Emergency Contacts:**
- Database DBA: @dba-lead (for data loss risk)
- Security Team: @security-lead (for security breaches)
- Product Owner: @product-owner (for business decisions)

***

## Final Checklist: Ready for Production

- [x] All teams trained on this guide
- [x] Slack channels created (#kanban-sync, #deployments, #api-updates, #incidents)
- [x] PagerDuty integrated with team-specific rotations
- [x] Jira Kanban board configured (swimlanes, WIP limits, DoR/DoD)
- [x] CODEOWNERS file created and enforced
- [x] OpenAPI validation in CI/CD (Spectral, oasdiff)
- [x] API contract testing framework (Pact + Pact Broker)
- [x] Kafka Schema Registry configured
- [x] Kafka topic naming convention documented
- [x] Deployment notifications to Slack working
- [x] Database migration pipeline (Flyway + notifications)
- [x] Incident response roles defined
- [x] Postmortem template in Confluence
- [x] Runbooks created (rollback, database, Kafka, API)
- [x] First incident drill completed
- [x] Mobile API compatibility testing process defined

**Sign-Off:**
- [x] Engineering Manager: _____________ Date: _______
- [x] Platform Lead: _________________ Date: _______
- [x] Core Team Lead: _______________ Date: _______
- [x] Retail Team Lead: _____________ Date: _______
- [x] AML Team Lead: ________________ Date: _______
- [x] Redesign Team Lead: ___________ Date: _______

***

**Document Version:** 2.0  
**Last Updated:** December 2025  
**Next Review:** March 2026  
**Owner:** Platform Team

**Teams:**
- **Core Team** (2 backend devs): user-service, auth-service, payment-service
- **Retail Team** (3 backend devs): product-catalog-service, cart-service, checkout-service, order-service
- **AML Team** (2 backend devs): transaction-monitor-service, compliance-report-service
- **Redesign Team** (1 backend dev): api-gateway, web-frontend