# Thesis Orientation Insertion

Buchin et al. (2016) introduce a simplification algorithm in their work that simplifies the input polygon while maintaining its area by moving edges. 
This algorithm is unable to introduce new orientations and thus can often not simplify staircase-like structures well. 
In the Master's Thesis that this project accompanies I have investigated how to augment the simplification algorithm such that it can introduce orientations where this would be beneficial. 
In the thesis I introduce multiple methods of rotating edges in the polygon to introduce new or existing orientations and investigate how they perform on both synthetic and real-world data. Augmenting the original algorithm with these methods allows it to more accurately simplify polygons without impacting performance.

## This project
This software implements the original simplification algorithm as an extendable framework. This framework has then been extended with the new introduction techniques I have researched. While the project is completely functional, actually extending the framework might require some explanation. The code is provided as is but feel free to contact me with any questions. If any of my work can be used for future developments that would be great and I'd gladly assist.
