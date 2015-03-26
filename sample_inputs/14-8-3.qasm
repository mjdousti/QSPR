#############################################################
####################Qubit declarations#######################
########################[[14,8,3]]###########################
#############################################################
qubit q0,0
qubit q1,0
qubit q2,0
qubit q3,0
qubit q4,0
qubit q5,0
qubit q6
qubit q7
qubit q8
qubit q9
qubit q10
qubit q11
qubit q12
qubit q13

#############################################################
####Hadamard operations to initialize the control section####
#############################################################
#Gate 0
h q0
#Gate 1
h q1
#Gate 2
h q2
#Gate 3
h q3
#Gate 4
h q4
#Gate 5
h q5
#############################################################
#########################CNOT Gates##########################
#############################################################
#Gate 6
cnot q6, q13
#Gate 7
cnot q8, q13
#Gate 8
cnot q10, q13
#Gate 9
cnot q12, q13
#############################################################
#Gate 10
cnot q13, q12
#############################################################
#Gate 11
cnot q12, q11
#Gate 12
cnot q13, q11
#Gate 13
cnot q10, q11
#Gate 14
cnot q6, q11
#############################################################
#Gate 15
cnot q8, q10
#Gate 16
cnot q11, q10
#Gate 17
cnot q12, q10
#Gate 18
cnot q13, q10
#############################################################
#Gate 19
cnot q8, q9
#Gate 20
cnot q10, q9
#Gate 21
cnot q12, q9
#Gate 22
cnot q13, q9
#############################################################
#Gate 23
cnot q6, q8
#Gate 24
cnot q7, q8
#Gate 25
cnot q9, q8
#Gate 26
cnot q10, q8
#Gate 27
cnot q11, q8
#Gate 28
cnot q12, q8
#Gate 29
cnot q13, q8
#############################################################
#Gate 30
cnot q8, q7
#Gate 31
cnot q9, q7
#Gate 32
cnot q10, q7
#Gate 33
cnot q11, q7
#Gate 34
cnot q12, q7
#############################################################
#Gate 35
cnot q10, q6
#Gate 36
cnot q12, q6
#############################################################
#Gate 37
cnot q7, q5
#Gate 38
cnot q8, q5
#Gate 39
cnot q9, q5
#Gate 40
cnot q11, q5
#Gate 41
cnot q12, q5
#Gate 42
cnot q13, q5
#############################################################
#Gate 43
cnot q6, q4
#Gate 44
cnot q7, q4
#Gate 45
cnot q8, q4
#Gate 46
cnot q10, q4
#Gate 47
cnot q11, q4
#Gate 48
cnot q12, q4
#Gate 49
cnot q13, q4
#############################################################
#Gate 50
cnot q4, q3
#Gate 51
cnot q5, q3
#Gate 52
cnot q7, q3
#Gate 53
cnot q8, q3
#Gate 54
cnot q9, q3
#Gate 55
cnot q10, q3
#Gate 56
cnot q11, q3
#Gate 57
cnot q12, q3
#############################################################
#Gate 58
cnot q4, q2
#Gate 59
cnot q5, q2
#Gate 60
cnot q6, q2
#Gate 61
cnot q8, q2
#Gate 62
cnot q9, q2
#Gate 63
cnot q10, q2
#Gate 64
cnot q11, q2
#Gate 65
cnot q12, q2
#Gate 66
cnot q13, q2
#############################################################
#Gate 67
cnot q3, q1
#Gate 68
cnot q4, q1
#Gate 69
cnot q5, q1
#Gate 70
cnot q7, q1
#Gate 71
cnot q8, q1
#Gate 72
cnot q9, q1
#Gate 73
cnot q10, q1
#Gate 74
cnot q11, q1
#Gate 75
cnot q12, q1
#############################################################
#Gate 76
cnot q3, q0
#Gate 77
cnot q5, q0
#Gate 78
cnot q6, q0
#Gate 79
cnot q7, q0
#Gate 80
cnot q9, q0
#Gate 81
cnot q10, q0
#Gate 82
cnot q11, q0
#Gate 83
cnot q12, q0
#Gate 84
cnot q13, q0
#############################################################
