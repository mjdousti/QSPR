#############################################################
####################Qubit declarations#######################
#######################[[23,1,7]]############################
#############################################################
qubit q0,0
qubit q1,0
qubit q2,0
qubit q3,0
qubit q4,0
qubit q5,0
qubit q6,0
qubit q7,0
qubit q8,0
qubit q9,0
qubit q10,0
qubit q11
qubit q12,0
qubit q13,0
qubit q14,0
qubit q15,0
qubit q16,0
qubit q17,0
qubit q18,0
qubit q19,0
qubit q20,0
qubit q21,0
qubit q22,0


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
#Gate 6
h q6
#Gate 7
h q7
#Gate 8
h q8
#Gate 9
h q9
#Gate 10
h q10

#############################################################
#Gate 11
cnot  q12, q11
#Gate 12
cnot  q16, q11
#Gate 13
cnot  q17, q11
#Gate 14
cnot  q18, q11
#Gate 15
cnot  q20, q11
#Gate 16
cnot  q22, q11
#############################################################

#############################################################
#Gate 17
cnot  q12, q10
#Gate 18
cnot  q15, q10
#Gate 19
cnot  q18, q10
#Gate 20
cnot  q19, q10
#Gate 21
cnot  q20, q10
#Gate 22
cnot  q21, q10
#Gate 23
cnot  q22, q10
#############################################################

#############################################################
#Gate 24
cnot  q11, q9
#Gate 25
cnot  q14, q9
#Gate 26
cnot  q17, q9
#Gate 27
cnot  q18, q9
#Gate 28
cnot  q19, q9
#Gate 29
cnot  q20, q9
#Gate 30
cnot  q21, q9
#############################################################

#############################################################
#Gate 31
cnot  q12, q8
#Gate 32
cnot  q13, q8
#Gate 33
cnot  q15, q8
#Gate 34
cnot  q16, q8
#Gate 35
cnot  q17, q8
#Gate 36
cnot  q21, q8
#Gate 37
cnot  q22, q8
#############################################################

#############################################################
#Gate 38
cnot  q11, q7
#Gate 39
cnot  q12, q7
#Gate 40
cnot  q14, q7
#Gate 41
cnot  q15, q7
#Gate 42
cnot  q16, q7
#Gate 43
cnot  q20, q7
#Gate 44
cnot  q21, q7
#############################################################

#############################################################
#Gate 45
cnot  q11, q6
#Gate 46
cnot  q12, q6
#Gate 47
cnot  q13, q6
#Gate 48
cnot  q14, q6
#Gate 49
cnot  q18, q6
#Gate 50
cnot  q21, q6
#Gate 51
cnot  q22, q6
#############################################################

#############################################################
#Gate 52
cnot  q11, q5
#Gate 53
cnot  q13, q5
#Gate 54
cnot  q15, q5
#Gate 55
cnot  q17, q5
#Gate 56
cnot  q18, q5
#Gate 57
cnot  q19, q5
#Gate 58
cnot  q22, q5
#############################################################

#############################################################
#Gate 59
cnot  q14, q4
#Gate 60
cnot  q15, q4
#Gate 61
cnot  q16, q4
#Gate 62
cnot  q17, q4
#Gate 63
cnot  q19, q4
#Gate 64
cnot  q20, q4
#Gate 65
cnot  q22, q4
#############################################################

#############################################################
#Gate 66
cnot  q13, q3
#Gate 67
cnot  q14, q3
#Gate 68
cnot  q15, q3
#Gate 69
cnot  q16, q3
#Gate 70
cnot  q18, q3
#Gate 71
cnot  q19, q3
#Gate 72
cnot  q21, q3
#############################################################

#############################################################
#Gate 73
cnot  q12, q2
#Gate 74
cnot  q13, q2
#Gate 75
cnot  q14, q2
#Gate 76
cnot  q15, q2
#Gate 77
cnot  q17, q2
#Gate 78
cnot  q18, q2
#Gate 79
cnot  q20, q2
#############################################################

#############################################################
#Gate 80
cnot  q11, q1
#Gate 81
cnot  q12, q1
#Gate 82
cnot  q13, q1
#Gate 83
cnot  q14, q1
#Gate 84
cnot  q16, q1
#Gate 85
cnot  q17, q1
#Gate 86
cnot  q19, q1
#############################################################

#############################################################
#Gate 87
cnot  q11, q0
#Gate 88
cnot  q13, q0
#Gate 89
cnot  q16, q0
#Gate 90
cnot  q19, q0
#Gate 91
cnot  q20, q0
#Gate 92
cnot  q21, q0
#Gate 93
cnot  q22, q0
#############################################################