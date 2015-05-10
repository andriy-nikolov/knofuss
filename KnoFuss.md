# Introduction #

KnoFuss is a system for semantic data fusion. It takes as input two semantic datasets represented in RDF and resolves the data linking problem:

  * Different datasets often contain information about the same entities but refer to them using different URIs. Such individuals have to be identified and either merged (by replacing URIs) or linked (e.g., using owl:sameAs relations). This problem is similar to the record linkage task studied in the database community.

# Main features #

KnoFuss provides a set of matching methods which can compare descriptions of RDF individuals and decide whether they refer to the same entity. KnoFuss is implemented as a modular and extensible architecture. The generic task of data linking is decomposed into two main subtasks:

  * Individual matching. This subtask compares properties of two individuals to decide whether they are likely to represent the same entity.
  * Dataset matching. At this stage, the whole set of candidate mappings produced by individual matching is analysed and refined. Thus, the system can capture the impact of different mappings on each other as well as the influence of ontological constraints: e.g., that a mapping between two individuals is less likely to be true if it leads to inconsistent data.


Each subtask of the fusion process can be performed by different methods, both generic and domain-dependent (e.g., using key attributes or machine-learning models for coreferencing, hand-tailored rules or formal ontology diagnosis for conflict detection). For both these subtasks, there are several techniques which can be applied: for example, string-based and set-based similarity metrics for individual matching, using ontological constraints and belief networks for dataset matching. Diifferent such methods can be plugged into the system and combined into a library, so that appropriate ones are selected depending on the task at hand.

In particular, the architecture contains the following methods:

  * Individual matching:
    1. Aggregated attribute-based similarity. This method uses the classical approach to individual matching where the similarity between individuals is calculated as an aggregation of similarities between their relevant attributes. The user can select the properties to be compared, similarity functions, weights, and the cut-off threshold.
    1. Unsupervised attribute-based similarity. This method also implements the aggregated attribute-based similarity. However, instead of relying on the user to choose the parameters of the combined similarity function, it tries to pick them automatically using a genetic algorithm. In the absence of reliable training data, it uses the desired distribution of resulting links to evaluate the fitness of candidate solutions: e.g., the expected number of mappings.
  * Dataset matching:
    1. Filtering based on ontological constraints. This method uses explicitly defined ontological constraints (class disjointness, functionality and cardinality restrictions) to update the original set of mappings provided by individual matching and filter out those which violate these constraints.
    1. Belief propagation network. This method combines uncertainty reasoning with ontological reasoning to refine the original set of mappings produced by individual matching methods. Confidence degrees of data statements in both repositories and of initial mappings are interpreted as Dempster-Shafer belief functions. Ontological reasoning is used to construct belief propagation networks capturing mutual impact of individual matching decisions. These networks are used to refine the original set of mappings.


# Usage #