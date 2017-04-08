#include <stdio.h>
#include <mpi.h>
#include <stdlib.h>
#include <unistd.h>


void multiply(int** mat,int* vec,int* ret);

int main(int argc,char** argv) {

	MPI_Init(NULL,NULL);
	int world_rank;
	MPI_Comm_rank(MPI_COMM_WORLD,&world_rank);
	int world_size;
	MPI_Comm_size(MPI_COMM_WORLD,&world_size);

	//read N and data from matrix and vector file


	//read vector
	int m=0,n=0;
	int** my_matrix;
	int* my_vec;
	int* ret;
	int* vec;
	int** matrix;
	if(world_rank==0){
		FILE *fp1,*fp2;
		fp1=fopen(argv[3],"r");
		fp2=fopen(argv[4],"r");
		if(fp1==0 || fp2==0){
			printf("file could not found\n");
		}
		vec=malloc(100000*sizeof(int));
		int i=0;
		while(fscanf(fp2,"%d",&vec[i])!=EOF){
			i++;
		}
		m=i;
		fscanf(fp1,"%d",&n);

		matrix=malloc(n*sizeof(int*));
		for(int i=0;i<n;i++){
			matrix[i]=malloc(m*sizeof(int));
		}
		i=0;
		int j=0;
		//read matrix
		for(i=0;i<n;i++){
			for(j=0;j<m;j++){
				if(fscanf(fp1,"%d",&matrix[i][j])==EOF){
					break;
				}
				printf("elem: %d",matrix[i][j]);
			}
			printf("\n");
		}
		fclose(fp1);
		fclose(fp2);

		//send n and m
		for(i=1;i<world_size;i++){
			MPI_Send(&n,1,MPI_INT,i,2,MPI_COMM_WORLD);
			MPI_Send(&m,1,MPI_INT,i,3,MPI_COMM_WORLD);
		}

	}

	if(world_rank>0){
		//receive n and m
		MPI_Recv(&n,1,MPI_INT,0,2,MPI_COMM_WORLD,MPI_STATUS_IGNORE);
		MPI_Recv(&m,1,MPI_INT,0,3,MPI_COMM_WORLD,MPI_STATUS_IGNORE);
	}

	//divide matrix 
	printf("number of processors..%d",world_size);
	int arr[world_size];
	int row_index[world_size];
	int quotient=n/world_size;
	int remainder=n%world_size;
	printf("quotien is...%d",quotient);
	printf("remainder is...%d",remainder);
	int i;
	for(i=0;i<world_size;i++){
		arr[i]=quotient;
	}

	for(i=0;i<remainder;i++){
		arr[i]+=1;
	}

	for(i=1;i<world_size;i++){
		row_index[i]+=arr[i];
	}

	if(world_rank==0){
		//send vector
		for(i=1;i<world_size;i++){
			MPI_Send(&vec[0],m,MPI_INT,i,0,MPI_COMM_WORLD);
		}

		//send matrix
		for(i=1;i<world_size;i++){
			MPI_Send(&matrix[row_index[i]][0],arr[i]*m,MPI_INT,i,1,MPI_COMM_WORLD);
		}

		//rank 0 vector and matrix
		my_vec=vec;
		my_matrix=malloc(arr[0]*sizeof(int*));
		for(i=0;i<arr[0];i++){
			my_matrix[i]=matrix[i];
		}

		//do multiplication
		ret=malloc(n*sizeof(int));
		multiply(my_matrix,my_vec,&ret[0]);
	} else {
		//receive vector and matrix
		MPI_Recv(&my_vec[0],m,MPI_INT,0,0,MPI_COMM_WORLD,MPI_STATUS_IGNORE);
		MPI_Recv(&my_matrix[0][0],arr[world_rank]*m,MPI_INT,0,1,MPI_COMM_WORLD,MPI_STATUS_IGNORE);

		//do multiplication
		int* tmp_ret=malloc(arr[world_rank]*sizeof(int));
		multiply(my_matrix,my_vec,tmp_ret);

		//Send result
		MPI_Send(&tmp_ret[0],arr[world_rank],MPI_INT,0,4,MPI_COMM_WORLD);
		free(tmp_ret);
	}

	if(world_rank==0){
		//receive result
		for(i=1;i<world_size;i++){
			MPI_Recv(&ret[row_index[i]],arr[i],MPI_INT,0,4,MPI_COMM_WORLD,MPI_STATUS_IGNORE);
		}

		//write result to file
		FILE* file=fopen("result.txt","w");
		if(file == NULL){
			fprintf(stderr,"open file error!\n");
		}

		for(i=0;i<n;i++){
			fprintf(file,"%d",ret[i]);
		}
	}

	free(ret);
	free(vec);
	free(matrix);
	free(my_matrix);
	free(my_vec);
	MPI_Finalize();
}


void multiply(int** mat,int* vec,int* ret) {
	int i,j;
	int n=sizeof(mat)/sizeof(mat[0]);
	int m=sizeof(vec)/sizeof(int);
	for(i=0;i<n;i++){
		ret[i]=0;
	}

	for(i=0;i<n;i++){
		for(j=0;j<m;j++){
			ret[i]+=mat[i][j]*vec[j];
		}
	}
}


