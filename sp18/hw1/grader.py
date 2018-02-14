def grader(filename):
    score_dict = {}
    with open(filename, 'r') as file:
        for line in file:
            field_list = line.rstrip('\n').split(' : ')
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


def write_file(filename, score_sort, score_merge, score_pdf):
    output_filename = filename.replace(".csv", "_grades.csv")
    output_file = open(output_filename, 'w')
    output_file.write("Student,ID,SIS User ID,SIS Login ID,Section,HW1 (4314282),HW2 (4325137),Assignments Current Points,Assignments Final Points,Assignments Current Score,Assignments Final Score,Current Points,Final Points,Current Score,Final Score\n")
    output_file.write("Points Possible,,,,,100.0,100.0,(read only),(read only),(read only),(read only),(read only),(read only),(read only),(read only)\n")

    with open(filename, 'r') as file:
        for line in file:
            if '\"' in line:
                field_list = line.split(',')
                id = field_list[3]
                score = 0
                if id in score_sort:
                    score = score + score_sort[id]
                    if id == 'bdn378':
					    print score
                if id in score_merge:
                    score = score + score_merge[id]
                    if id == 'bdn378':
					    print score_merge[id]
                if id in score_pdf:
                   score = score + score_pdf[id]
                   if id == 'bdn378':
					    print score_pdf[id]
                if id == 'bdn378':
					print score
                field_list[6] = str(score)
                output_file.write(",".join(field_list))

    output_file.close()



if __name__ == "__main__":
    sort = "scores.txt"
    merge = "scoresMerge.txt"
    result = "results.csv"
    score_sort = grader(sort)
    score_merge = grader(merge)
    score = grader2(result)

    filename2 = "../2018-02-08T1459_Grades-E_E_360P.csv"
    write_file(filename2, score_sort, score_merge, score)
