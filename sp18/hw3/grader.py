def grader(filename):
    score_dict = {}
    with open(filename, 'r') as file:
        for line in file:
            field_list = line.rstrip('\n').split(': ')
            score = int(field_list[-1])
            info_list = field_list[0].split('_')
            id1 = info_list[-2].lstrip('[').split('-')[0].lower()
            id2 = info_list[-1].rstrip(']').split('-')[0].lower()
            if id1 not in score_dict:
                score_dict[id1] = score
            if id2 not in score_dict:
                score_dict[id2] = score

    return score_dict

def grader2(filename):
    score_dict = {}
    with open(filename, 'r') as file:
        for line in file:
            field_list = line.rstrip('\n').split(' : ')
            score = int(field_list[-1])
            id = field_list[0]
            score_dict[id] = score

    return score_dict


def write_file(filename, scores):
    output_filename = filename.replace(".csv", "_grades.csv")
    output_file = open(output_filename, 'w')
    output_file.write("Student,ID,SIS User ID,SIS Login ID,Section,HW1 (4314282),HW2 (4325137),HW3 (4333716),HW 4 (4341513),Test 1 (4334134),Assignments Current Points,Assignments Final Points,Assignments Current Score,Assignments Unposted Current Score,Assignments Final Score,Assignments Unposted Final Score,Exams Current Points,Exams Final Points,Exams Current Score,Exams Unposted Current Score,Exams Final Score,Exams Unposted Final Score,Current Points,Final Points,Current Score,Unposted Current Score,Final Score,Unposted Final Score\n")
    output_file.write(",,,,,,,Muted,,,,,,,,,,,,,,,,,,,,\n");
    output_file.write("Points Possible,,,,,100.0,100.0,100.0,100.0,75.0,(read only),(read only),(read only),(read only),(read only),(read only),(read only),(read only),(read only),(read only),(read only),(read only),(read only),(read only),(read only),(read only),(read only),(read only)\n");

    with open(filename, 'r') as file:
        for line in file:
            if '\"' in line:
                field_list = line.split(',')
                id = field_list[3]
                try:
                    score = float(field_list[8])
                except ValueError:
                    score = 0
                if id in scores:
                    score = score + scores[id]
                field_list[8] = str(score)
                output_file.write(",".join(field_list))

    output_file.close()



if __name__ == "__main__":
    score = "scores.txt"
    result = "results.csv"
    score_final= grader(score)
    filename = "2018-03-28T1434_Grades-E_E_360P.csv"
    write_file(filename, score_final)	
    with open("scores_prog.txt", "wb") as f:
        for key in score_final:
            f.write(key+": "+str(score_final[key])+"\n")


