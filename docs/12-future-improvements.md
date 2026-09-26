# 12. Future improvements and rollout boundaries

The repository provides working API and Android feature paths. Production release still requires environment-specific identity provisioning, ERP master-data import, real Firebase credentials, enterprise signing and device acceptance testing. No deployment to a live store has been performed.

- Add user/store administration, device credential rotation/revocation, enterprise SSO and managed-device enrollment. The current bootstrap handles only a first administrator.
- Add asymmetric JWT keys with overlapping rotation and centrally managed refresh-session revocation. Add per-account throttling alongside ingress limits for distributed login abuse.
- Implement your actual ERP adapter and image-object lifecycle, with reconciliation and monitoring. The implemented boundary is versioned image metadata ingestion plus a store-scoped pull ledger; no proprietary ERP integration is presumed.
- Replace per-store writer serialization with CDC or an ordered transactional dispatch stream if measured inventory throughput requires it.
- Expand Android instrumentation coverage with an emulator/device farm, accessibility/localization, multi-line disposal editing, configurable scan-order limits and role-aware action visibility. The backend already enforces permissions; currently disallowed actions return 403.
- Add dashboards, alert policies, scheduled retention and audit export jobs. Define the audit retention and backup recovery objectives with operations.
- Certify vendor scanners and background alarms on the actual hardware/firmware. Add licensed SDK adapters where intent output is unavailable.
- Review dependency support/security updates before release, pin container digests and sign build artifacts. The pinned stack is buildable; it has not undergone a penetration test, vulnerability certification or store load test.
