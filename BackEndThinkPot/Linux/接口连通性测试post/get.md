# POST  请求 
```bash
# 一行命令
curl -i -X POST "https://sucms.xdf.cn/znq/api/seat/getByEmpId" -H "Content-Type: application/json" -H  "Schoolcode: 23" 
-d '{"empId":"466350"}'

```

```bash
# 一行命令
curl -i -X POST "https://sucms.xdf.cn/znq/api/letter/getByEmail" -H "Content-Type: application/json" -H  "Schoolcode: 23" 
-d '{"empId":"101898348876793526"}'

```


# Get  请求
```bash
curl -k -i https://sucms.xdf.cn/znq/api/seat/getByEmpId?&empId=466350 
```