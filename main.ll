@msg = internal constant [13 x i8] c"Hello World!\00"
@.i32 = private unnamed_addr constant [3 x i8] c"%d\00"
align 1 
declare i32 @scanf(i8*, ...)
declare i32 @printf(i8*, ...)

define i32 @main(i32 %a ) {
    ;call i32 @puts(i8* getelementptr inbounds ([13 x i8], [13 x i8]* @msg, i32 0, i32 0))
    ret i32 0
}
