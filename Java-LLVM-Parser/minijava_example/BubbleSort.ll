@.BubbleSort_vtable = global [0 x i8*] []

@.BBS_vtable = global [4 x i8*] [
	i8* bitcast (i32 (i8*,i32)* @BBS.Start to i8*),
	i8* bitcast (i32 (i8*)* @BBS.Sort to i8*),
	i8* bitcast (i32 (i8*)* @BBS.Print to i8*),
	i8* bitcast (i32 (i8*,i32)* @BBS.Init to i8*)
]

declare i8* @calloc(i32, i32)
declare i32 @printf(i8*, ...)
declare void @exit(i32)

@_cint = constant [4 x i8] c"%d\0a\00"
@_cOOB = constant [15 x i8] c"Out of bounds\0a\00"
@_cNSZ = constant [15 x i8] c"Negative size\0a\00"

define void @print_int(i32 %i) {
	%_str = bitcast [4 x i8]* @_cint to i8*
	call i32 (i8*, ...) @printf(i8* %_str, i32 %i)
	ret void
}

define void @throw_oob() {
	%_str = bitcast [15 x i8]* @_cOOB to i8*
	call i32 (i8*, ...) @printf(i8* %_str)
	call void @exit(i32 1)
	ret void
}

define void @throw_nsz() {
	%_str = bitcast [15 x i8]* @_cNSZ to i8*
	call i32 (i8*, ...) @printf(i8* %_str)
	call void @exit(i32 1)
	ret void
}

define i32 @main() {
	%_0 = call i8* @calloc(i32 1, i32 20)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [4 x i8*], [4 x i8*]* @.BBS_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i32 (i8*,i32)*
	%_8 = call i32 %_7(i8* %_0, i32 10)
	call void (i32) @print_int(i32 %_8)
	ret i32 0
}

define i32 @BBS.Start(i8* %this, i32 %.sz) {
	%sz = alloca i32
	store i32 %.sz, i32* %sz
	%aux01 = alloca i32
	%_0 = load i32, i32* %sz
	%_1 = bitcast i8* %this to i8***
	%_2 = load i8**, i8*** %_1
	%_3 = getelementptr i8*, i8** %_2, i32 3
	%_4 = load i8*, i8** %_3
	%_5 = bitcast i8* %_4 to i32 (i8*,i32)*
	%_6 = call i32 %_5(i8* %this, i32 %_6)
	store i32 %_6, i32* %aux01
	%_7 = bitcast i8* %this to i8***
	%_8 = load i8**, i8*** %_7
	%_9 = getelementptr i8*, i8** %_8, i32 2
	%_10 = load i8*, i8** %_9
	%_11 = bitcast i8* %_10 to i32 (i8*)*
	%_12 = call i32 %_11(i8* %this)
	store i32 %_12, i32* %aux01
	call void (i32) @print_int(i32 99999)
	%_13 = bitcast i8* %this to i8***
	%_14 = load i8**, i8*** %_13
	%_15 = getelementptr i8*, i8** %_14, i32 1
	%_16 = load i8*, i8** %_15
	%_17 = bitcast i8* %_16 to i32 (i8*)*
	%_18 = call i32 %_17(i8* %this)
	store i32 %_18, i32* %aux01
	%_19 = bitcast i8* %this to i8***
	%_20 = load i8**, i8*** %_19
	%_21 = getelementptr i8*, i8** %_20, i32 2
	%_22 = load i8*, i8** %_21
	%_23 = bitcast i8* %_22 to i32 (i8*)*
	%_24 = call i32 %_23(i8* %this)
	store i32 %_24, i32* %aux01
	ret i32 0
}

define i32 @BBS.Sort(i8* %this) {
	%nt = alloca i32
	%i = alloca i32
	%aux02 = alloca i32
	%aux04 = alloca i32
	%aux05 = alloca i32
	%aux06 = alloca i32
	%aux07 = alloca i32
	%j = alloca i32
	%t = alloca i32
	%_0 = getelementptr i8, i8* %this, i32 16
	%_1 = bitcast i8* %_0 to i32*
	%_2 = load i32, i32* %_1
	%_3 = sub i32 %_2, 1
	%_4 = sub i32 0, 1
	br label %loop0
	loop0:
	%_5 = load i32, i32* %aux02
	%_6 = load i32, i32* %i
	%_7 = icmp slt i32 %_5, %_6
	br i1 %_7, label %loop1, label %loop2
	loop1:
	store i32 1, i32* %j
	br label %loop2
	loop2:
	%_8 = load i32, i32* %j
	%_9 = load i32, i32* %i
	%_10 = add i32 %_9, 1
	%_11 = icmp slt i32 %_8, %_10
	br i1 %_11, label %loop3, label %loop4
	loop3:
	%_12 = load i32, i32* %j
	%_13 = sub i32 %_12, 1
	%_14 = load i32, i32* %aux04
	%_15 = getelementptr i8, i8* %this, i32 8
	%_16 = bitcast i8* %_15 to i32**
	%_17 = load i32*, i32** %_16
	%_18 = load i32, i32* %_17
	%_19 = icmp sge i32 %_14, 0
	%_20 = icmp slt i32 %_14, %_18
	%_21 = and i1 %_19, %_20
	br i1 %_21, label %oob_ok_0, label %oob_err_0
	oob_err_0:
	call void @throw_oob()
	br label %oob_ok_0
	oob_ok_0:
	%_22 = add i32 1, 14
	%_23 = getelementptr i32, i32* %_17, i32 %_22
	%_24 = load i32, i32* %_23
	%_25 = load i32, i32* %aux05
	%_26 = getelementptr i8, i8* %this, i32 8
	%_27 = bitcast i8* %_26 to i32**
	%_28 = load i32*, i32** %_27
	%_29 = load i32, i32* %_28
	%_30 = icmp sge i32 %_25, 0
	%_31 = icmp slt i32 %_25, %_29
	%_32 = and i1 %_30, %_31
	br i1 %_32, label %oob_ok_1, label %oob_err_1
	oob_err_1:
	call void @throw_oob()
	br label %oob_ok_1
	oob_ok_1:
	%_33 = add i32 1, 25
	%_34 = getelementptr i32, i32* %_28, i32 %_33
	%_35 = load i32, i32* %_34
	%_36 = load i32, i32* %aux05
	%_37 = load i32, i32* %aux04
	%_38 = icmp slt i32 %_36, %_37
	br i1 %_38, label %if_then_0, label %if_else_0
	if_else_0:
	store i32 0, i32* %nt
	br label %if_end_0
	if_then_0:
	%_39 = load i32, i32* %j
	%_40 = sub i32 %_39, 1
	%_41 = load i32, i32* %t
	%_42 = getelementptr i8, i8* %this, i32 8
	%_43 = bitcast i8* %_42 to i32**
	%_44 = load i32*, i32** %_43
	%_45 = load i32, i32* %_44
	%_46 = icmp sge i32 %_41, 0
	%_47 = icmp slt i32 %_41, %_45
	%_48 = and i1 %_46, %_47
	br i1 %_48, label %oob_ok_2, label %oob_err_2
	oob_err_2:
	call void @throw_oob()
	br label %oob_ok_2
	oob_ok_2:
	%_49 = add i32 1, 41
	%_50 = getelementptr i32, i32* %_44, i32 %_49
	%_51 = load i32, i32* %_50
	%_52 = getelementptr i8, i8* %this, i32 8
	%_53 = bitcast i8* %_number to i32*
	%_54 = load i32, i32* %_number
	%_55 = getelementptr i8, i8* %this, i32 8
	%_56 = bitcast i8* %_55 to i32**
	%_57 = load i32*, i32** %_56
	%_58 = load i32, i32* %_57
	%_59 = icmp sge i32 %_54, 0
	%_60 = icmp slt i32 %_54, %_58
	%_61 = and i1 %_59, %_60
	br i1 %_61, label %oob_ok_3, label %oob_err_3
	oob_err_3:
	call void @throw_oob()
	br label %oob_ok_3
	oob_ok_3:
	%_62 = add i32 1, 54
	%_63 = getelementptr i32, i32* %_57, i32 %_62
	%_64 = load i32, i32* %_63
	%_65 = getelementptr i8, i8* %this, i32 8
	%_66 = bitcast i8* %_number to i32*
	%_67 = load i32, i32* %_number
	%_68 = load i32*, i32** %t
	%_69 = load i32, i32* %_68
	%_70 = icmp sge i32 %_67, 0
	%_71 = icmp slt i32 %_67, %_69
	%_72 = and i1 %_70, %_71
	br i1 %_72, label %oob_ok_4, label %oob_err_4
	oob_err_4:
	call void @throw_oob()
	br label %oob_ok_4
	oob_ok_4:
	%_73 = add i32 1, 67
	%_74 = getelementptr i32, i32* %_68, i32 %_73
	%_75 = load i32, i32* %_74
	br label %if_end_0
	if_end_0:
	%_76 = load i32, i32* %j
	%_77 = add i32 %_76, 1
	store i32 %_77, i32* %j
	store i32 %_77, i32* %j
	br label %loop2
	loop4:
	%_78 = load i32, i32* %i
	%_79 = sub i32 %_78, 1
	br label %loop3
	loop5:
	ret i32 0
}

define i32 @BBS.Print(i8* %this) {
	%j = alloca i32
	store i32 0, i32* %j
	br label %loop6
	loop6:
	%_0 = load i32, i32* %j
	%_1 = getelementptr i8, i8* %this, i32 16
	%_2 = bitcast i8* %_1 to i32*
	%_3 = load i32, i32* %_2
	%_4 = icmp slt i32 %_0, %_3
	br i1 %_4, label %loop7, label %loop8
	loop7:
	%_5 = load i32, i32* %j
	%_6 = getelementptr i8, i8* %this, i32 8
	%_7 = bitcast i8* %_6 to i32**
	%_8 = load i32*, i32** %_7
	%_9 = load i32, i32* %_8
	%_10 = icmp sge i32 %_5, 0
	%_11 = icmp slt i32 %_5, %_9
	%_12 = and i1 %_10, %_11
	br i1 %_12, label %oob_ok_5, label %oob_err_5
	oob_err_5:
	call void @throw_oob()
	br label %oob_ok_5
	oob_ok_5:
	%_13 = add i32 1, 5
	%_14 = getelementptr i32, i32* %_8, i32 %_13
	%_15 = load i32, i32* %_14
	call void (i32) @print_int(i32 %_15)
	%_16 = load i32, i32* %j
	%_17 = add i32 %_16, 1
	store i32 %_17, i32* %j
	store i32 %_17, i32* %j
	br label %loop6
	loop8:
	ret i32 0
}

define i32 @BBS.Init(i8* %this, i32 %.sz) {
	%sz = alloca i32
	store i32 %.sz, i32* %sz
	%_0 = load i32, i32* %sz
	%_1 = getelementptr i8, i8* %this, i32 16
	%_2 = bitcast i8* %_1 to i32*
	store i32 %_0, i32* %_2
	%_3 = load i32, i32* %sz
	%_4 = add i32 1, %_3
	%_5 = icmp sge i32 %_4, 1
	br i1 %_5, label %nsz_ok_0, label %nsz_err_0
	nsz_err_0:
	call void @throw_nsz()
	br label %nsz_ok_0
	nsz_ok_0:
	%_6 = call i8 * @calloc(i32 %_4, i32 4)
	%_7 = bitcast i8* %_6 to i32*
	store i32 4, i32* %_7
	%_8 = getelementptr i8, i8* %this, i32 8
	%_9 = bitcast i8* %_8 to i32**
	store i32* %_7, i32** %_9
	%_10 = load i32*, i32** %number
	%_11 = load i32, i32* %_10
	%_12 = icmp sge i32 0, 0
	%_13 = icmp slt i32 0, %_11
	%_14 = and i1 %_12, %_13
	br i1 %_14, label %oob_ok_6, label %oob_err_6
	oob_err_6:
	call void @throw_oob()
	br label %oob_ok_6
	oob_ok_6:
	%_15 = add i32 1, 0
	%_16 = getelementptr i32, i32* %_10, i32 %_15
	store i32 20, i32* %_16
	%_17 = load i32*, i32** %number
	%_18 = load i32, i32* %_17
	%_19 = icmp sge i32 1, 0
	%_20 = icmp slt i32 1, %_18
	%_21 = and i1 %_19, %_20
	br i1 %_21, label %oob_ok_7, label %oob_err_7
	oob_err_7:
	call void @throw_oob()
	br label %oob_ok_7
	oob_ok_7:
	%_22 = add i32 1, 1
	%_23 = getelementptr i32, i32* %_17, i32 %_22
	store i32 7, i32* %_23
	%_24 = load i32*, i32** %number
	%_25 = load i32, i32* %_24
	%_26 = icmp sge i32 2, 0
	%_27 = icmp slt i32 2, %_25
	%_28 = and i1 %_26, %_27
	br i1 %_28, label %oob_ok_8, label %oob_err_8
	oob_err_8:
	call void @throw_oob()
	br label %oob_ok_8
	oob_ok_8:
	%_29 = add i32 1, 2
	%_30 = getelementptr i32, i32* %_24, i32 %_29
	store i32 12, i32* %_30
	%_31 = load i32*, i32** %number
	%_32 = load i32, i32* %_31
	%_33 = icmp sge i32 3, 0
	%_34 = icmp slt i32 3, %_32
	%_35 = and i1 %_33, %_34
	br i1 %_35, label %oob_ok_9, label %oob_err_9
	oob_err_9:
	call void @throw_oob()
	br label %oob_ok_9
	oob_ok_9:
	%_36 = add i32 1, 3
	%_37 = getelementptr i32, i32* %_31, i32 %_36
	store i32 18, i32* %_37
	%_38 = load i32*, i32** %number
	%_39 = load i32, i32* %_38
	%_40 = icmp sge i32 4, 0
	%_41 = icmp slt i32 4, %_39
	%_42 = and i1 %_40, %_41
	br i1 %_42, label %oob_ok_10, label %oob_err_10
	oob_err_10:
	call void @throw_oob()
	br label %oob_ok_10
	oob_ok_10:
	%_43 = add i32 1, 4
	%_44 = getelementptr i32, i32* %_38, i32 %_43
	store i32 2, i32* %_44
	%_45 = load i32*, i32** %number
	%_46 = load i32, i32* %_45
	%_47 = icmp sge i32 5, 0
	%_48 = icmp slt i32 5, %_46
	%_49 = and i1 %_47, %_48
	br i1 %_49, label %oob_ok_11, label %oob_err_11
	oob_err_11:
	call void @throw_oob()
	br label %oob_ok_11
	oob_ok_11:
	%_50 = add i32 1, 5
	%_51 = getelementptr i32, i32* %_45, i32 %_50
	store i32 11, i32* %_51
	%_52 = load i32*, i32** %number
	%_53 = load i32, i32* %_52
	%_54 = icmp sge i32 6, 0
	%_55 = icmp slt i32 6, %_53
	%_56 = and i1 %_54, %_55
	br i1 %_56, label %oob_ok_12, label %oob_err_12
	oob_err_12:
	call void @throw_oob()
	br label %oob_ok_12
	oob_ok_12:
	%_57 = add i32 1, 6
	%_58 = getelementptr i32, i32* %_52, i32 %_57
	store i32 6, i32* %_58
	%_59 = load i32*, i32** %number
	%_60 = load i32, i32* %_59
	%_61 = icmp sge i32 7, 0
	%_62 = icmp slt i32 7, %_60
	%_63 = and i1 %_61, %_62
	br i1 %_63, label %oob_ok_13, label %oob_err_13
	oob_err_13:
	call void @throw_oob()
	br label %oob_ok_13
	oob_ok_13:
	%_64 = add i32 1, 7
	%_65 = getelementptr i32, i32* %_59, i32 %_64
	store i32 9, i32* %_65
	%_66 = load i32*, i32** %number
	%_67 = load i32, i32* %_66
	%_68 = icmp sge i32 8, 0
	%_69 = icmp slt i32 8, %_67
	%_70 = and i1 %_68, %_69
	br i1 %_70, label %oob_ok_14, label %oob_err_14
	oob_err_14:
	call void @throw_oob()
	br label %oob_ok_14
	oob_ok_14:
	%_71 = add i32 1, 8
	%_72 = getelementptr i32, i32* %_66, i32 %_71
	store i32 19, i32* %_72
	%_73 = load i32*, i32** %number
	%_74 = load i32, i32* %_73
	%_75 = icmp sge i32 9, 0
	%_76 = icmp slt i32 9, %_74
	%_77 = and i1 %_75, %_76
	br i1 %_77, label %oob_ok_15, label %oob_err_15
	oob_err_15:
	call void @throw_oob()
	br label %oob_ok_15
	oob_ok_15:
	%_78 = add i32 1, 9
	%_79 = getelementptr i32, i32* %_73, i32 %_78
	store i32 5, i32* %_79
	ret i32 0
}
