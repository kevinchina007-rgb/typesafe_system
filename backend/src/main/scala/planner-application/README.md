Stage 2 planner-application skeleton.

Included now:
- PlannerService orchestration skeleton
- CandidateGenerator trait
- ConstraintChecker trait
- CandidateScorer trait
- PlannerDraftRepository trait
- ResourceMatcher trait
- FakeCandidateGenerator example
- InMemoryPlannerDraftRepository for local iteration
- PlannerApplicationExample runnable sample

TODO(stage-3):
- replace permissive constraint checker with structured constraint validation
- preserve richer validation warnings and violations

TODO(stage-4):
- generate multiple strategy-based candidates
- split recommended vs alternatives using richer scoring
