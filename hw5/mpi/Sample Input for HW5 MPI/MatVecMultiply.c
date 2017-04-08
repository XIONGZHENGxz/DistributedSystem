#include <stdio.h>
#include <mpi.h>
#include <stdlib.h>
#include <unistd.h>


void multiply(int n,int m,int** mat,int* vec,int* ret);
void divide(int n,int numProc,int* arr,int* row_index);

int main(int argc,char** argv) {

	MPI_Init(NULL,NULL);
	int world_rank;
	MPI_Comm_rank(MPI_COMM_WORLD,&world_rank);
	int world_size;
	MPI_Comm_size(MPI_COMM_WORLD,&world_size);

	int m=0,n=0;
	int* ret;
	int* vec;
	int** my_matrix;
	int* my_vec;
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
				//			printf("elem: %d",matrix[i][j]);
			}
			//		printf("\n");
		}

		fclose(fp1);
		fclose(fp2);

		//send n and m
		printf("broadcast...m and n\n");
		MPI_Bcast(&n,1,MPI_INT,0,MPI_COMM_WORLD);
		MPI_Bcast(&m,1,MPI_INT,0,MPI_COMM_WORLD);
		int* arr=malloc(world_size*sizeof(int));
		int* row_index=malloc(world_size*sizeof(int));

		//send vector
		MPI_Bcast(&vec[0],m,MPI_INT,0,MPI_COMM_WORLD);
		printf("sent vec...\n");

		//divide matrix
		divide(n,world_size,arr,row_index);
		printf("matrix divided...%d\n",row_index[1]);

		//send matrix
		for(i=1;i<world_size;i++){
			int** mat=malloc(arr[i]*sizeof(int*));
			for(j=0;j<arr[i];j++){
				mat[j]=malloc(m*sizeof(int));
			}
			int k;
			for(k=0;k<arr[i];k++){
				for(j=0;j<m;j++){
					mat[k][j]=matrix[row_index[i]+k][j];
					//		printf("mat...%d ",mat[k][j]);
				}
				//	printf("\n");
			}
			for(k=0;k<arr[i];k++){
				MPI_Send(&(mat[k][0]),m,MPI_INT,i,1,MPI_COMM_WORLD);
			}
			free(mat);
		}
		printf("sent matrix...\n");

		//rank 0 vector and matrix
		my_vec=vec;
		my_matrix=malloc(arr[0]*sizeof(int*));
		for(i=0;i<arr[0];i++){
			my_matrix[i]=malloc(m*sizeof(int));
		}
		for(i=0;i<arr[0];i++){
			for(j=0;j<m;j++) {
				my_matrix[i][j]=matrix[i][j];
		//		printf("my_matrix...%d ",my_matrix[i][j]);
			}
		//	printf("\n");
		}
		//do multiplication
		ret=malloc(n*sizeof(int));
		multiply(arr[0],m,my_matrix,my_vec,ret);

		//receive result
		for(i=1;i<world_size;i++){
			MPI_Recv(&(ret[row_index[i]]),arr[i],MPI_INT,i,4,MPI_COMM_WORLD,MPI_STATUS_IGNORE);
		}
		printf("result received...\n");
		//write result to file
		FILE* file=fopen("result.txt","w");
		if(file == NULL){
			fprintf(stderr,"open file error!\n");
		}

		for(i=0;i<n;i++){
			fprintf(file,"%d\n",ret[i]);
		}
		fclose(file);
		free(arr);
		free(row_index);
	} else {
		//receive n and m
		MPI_Bcast(&n,1,MPI_INT,0,MPI_COMM_WORLD);
		MPI_Bcast(&m,1,MPI_INT,0,MPI_COMM_WORLD);

		//MPI_Recv(&m,1,MPI_INT,0,3,MPI_COMM_WORLD,MPI_STATUS_IGNORE);
		printf("receive n and m...%d,%d\n",n,m);
		int* arr=malloc(world_size*sizeof(int));
		int* row_index=malloc(world_size*sizeof(int));

		//divide matrix
		divide(n,world_size,arr,row_index);
		printf("divided....\n");

		//receive vector and matrix
		my_vec=malloc(m*sizeof(int));
		my_matrix=malloc(arr[world_rank]*sizeof(int*));
		int i;
		for(i=0;i<arr[world_rank];i++){
			my_matrix[i]=malloc(m*sizeof(int));
		}
		MPI_Bcast(&my_vec[0],m,MPI_INT,0,MPI_COMM_WORLD);
		printf("received vec...\n");
		for(i=0;i<arr[i];i++){
			MPI_Recv(&(my_matrix[i][0]),m,MPI_INT,0,1,MPI_COMM_WORLD,MPI_STATUS_IGNORE);
		}
		printf("received matrix...\n");	
		int j;

		/* show matrix 
		   for(i=0;i<arr[world_rank];i++){
		   for(j=0;j<m;j++){
		   printf("rank1 matrx....%d ",my_matrix[i][j]);
		   }
		   printf("\n");
		   }
		 */

		//do multiplication
		int* tmp_ret=malloc(arr[world_rank]*sizeof(int));
		multiply(arr[world_rank],m,my_matrix,my_vec,tmp_ret);

		//Send result
		MPI_Send(&tmp_ret[0],arr[world_rank],MPI_INT,0,4,MPI_COMM_WORLD);
		printf("result sent...\n");
		free(tmp_ret);
		free(arr);
		free(row_index);
		free(my_vec);
		free(my_matrix);
	}
	free(ret);
	free(vec);
	free(matrix);
	MPI_Finalize();
}


void multiply(int n,int m,int** mat,int* vec,int* ret) {
	int i,j;
	for(i=0;i<n;i++){
		ret[i]=0;
	}

	for(i=0;i<n;i++){
		for(j=0;j<m;j++){
			ret[i]+=mat[i][j]*vec[j];
		}
	}
}

void divide(int n,int numProc,int* arr,int* row_index) {
	int quotient=n/numProc;
	int remainder=n%numProc;
	int i;
	for(i=0;i<numProc;i++){
		arr[i]=quotient;
		row_index[i]=0;
	}

	for(i=0;i<remainder;i++){
		arr[i]+=1;
	}

	for(i=1;i<numProc;i++){
		row_index[i]+=arr[i];
	}
}	
