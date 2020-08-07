import requests

students = "http://localhost:8080/API/students/"
courses = "http://localhost:8080/API/courses/"
# vms = "http://localhost:8080/API/vms/"


def addStudent(id, firstName, name, token):
    json = {"id": id, "firstName": firstName, "name": name}
    print("POST TO: " + students)
    return requests.post(students, json=json, headers=get_headers(token))


def addCourse(name, min, max, token):
    json = {"name": name, "min": min, "max": max}
    print("POST TO: " + courses)
    return requests.post(courses, json=json, headers=get_headers(token))


def enrollOne(course, studentId, token):
    url = f"{courses}{course}/enrollOne"
    print("POST TO: " + url)
    return requests.post(url, data=studentId, headers=get_headers(token))


def proposeTeam(courseName, teamName, memberIds, token):
    url = f"{courses}{courseName}/proposeTeam"
    print("POST TO: " + url)
    json = {"teamName": teamName, "memberIds": list(memberIds)}
    return requests.post(url, json=json, headers=get_headers(token))


def enableDisableCourse(courseName, available, token):
    uri = "enable" if available is True else "disable"
    url = f"{courses}{courseName}/{uri}"
    print("PUT TO: " + url)
    return requests.put(url, headers=get_headers(token))


def authenticate(username, password):
    url = "http://localhost:8080/authenticate"
    print("POST TO: " + url)
    return requests.post(url, json={"username": username, "password": password})


def get_headers(token):
    return {"Content-Type": "application/json", "Authorization": f"Bearer {token}"}


def me(token):
    url = "http://localhost:8080/me"
    return requests.get(url, headers=get_headers(token))


def all_courses(token):
    url = courses
    return requests.get(url, headers=get_headers(token))


def authenticate_users(s_account, d_account):
    d1 = authenticate(d_account['username'], d_account['password'])
    s1 = authenticate(s_account['username'], s_account['password'])
    admin = authenticate("admin", "admin")
    print(d1.text)
    print(s1.text)
    print(admin.text)


def addProfessor(id, firstName, name, token):
    url = "http://localhost:8080/API/admin/addProfessor"
    json = {"id": id, "firstName": firstName, "name": name}
    print("POST TO: " + url)
    return requests.post(url, json=json, headers=get_headers(token))


def createVMModel(os, version, course_name, token):
    url = f"{courses}{course_name}/vm-model"
    json = {"os": os, "version": version}
    print("POST TO: " + url)
    return requests.post(url, json=json, headers=get_headers(token))


def updateVMModel(os, version, course_name, token):
    url = f"{courses}{course_name}/vm-model"
    json = {"os": os, "version": version}
    print("PUT TO: " + url)
    return requests.put(url, json=json, headers=get_headers(token))


def createVmConfiguration(course_name, team_id, max_cpu, max_ram, max_disk, max_active, max_vm, token):
    url = f"{courses}{course_name}/teams/{team_id}/vm-config"
    json = {"maxCpu": max_cpu, "maxDisk": max_disk, "maxRam": max_ram, "maxActive": max_active,
            "maxVm": max_vm}
    print("PUT TO: " + url)
    return requests.post(url, json=json, headers=get_headers(token))

def updateVmConfiguration(course_name, team_id, max_cpu, max_ram, max_disk, max_active, max_vm, token):
    url = f"{courses}{course_name}/teams/{team_id}/vm-config"
    json = {"maxCpu": max_cpu, "maxDisk": max_disk, "maxRam": max_ram, "maxActive": max_active,
            "maxVm": max_vm}
    print("PUT TO: " + url)
    return requests.put(url, json=json, headers=get_headers(token))


def createVmInstance(course_name, team_id, student_id, cpu, ram, disk, token):
    url = f"{courses}{course_name}/teams/{team_id}/vms"
    json = {
        "studentId": student_id,
        "instance": {
            "cpu": cpu,
            "ramSize": ram,
            "diskSize": disk
        }
    }
    print("POST TO: " + url)
    return requests.post(url, json=json, headers=get_headers(token))


def bootVM(course_name, team_id, vm_id, owner_id, token):
    url = f"{courses}{course_name}/teams/{team_id}/vms/{vm_id}/boot"
    print("PUT TO: " + url)
    return requests.put(url, data=owner_id, headers=get_headers(token))


def shutdownVM(course_name, team_id, vm_id, owner_id, token):
    url = f"{courses}{course_name}/teams/{team_id}/vms/{vm_id}/shutdown"
    print("PUT TO: " + url)
    return requests.put(url, data=owner_id, headers=get_headers(token))


def deleteVM(course_name, team_id, vm_id, owner_id, token):
    url = f"{courses}{course_name}/teams/{team_id}/vms/{vm_id}"
    print("PUT TO: " + url)
    return requests.delete(url, data=owner_id, headers=get_headers(token))


if __name__ == '__main__':
    s1_account = {"username": "s1@studenti.polito.it", "password": "DmeJv.6f-0"}
    s2_account = {"username": "s2@studenti.polito.it", "password": "6SqP,t6D1%"}
    d1_account = {"username": "d1@polito.it", "password": "DfC&O3N0-l"}
    d2_account = {"username": "d2@polito.it", "password": '2%Ts1N"sRa'}
    admin_token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIlJPTEVfQURNSU4iXSwiaWF0IjoxNTk2NjM2MzY1LCJleHAiOjE1OTY2NzIzNjV9.ekAVceJSjCsvMTHA02QVoDmyGUfPnvRUC2JVB3-kukg"
    d1_token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkMUBwb2xpdG8uaXQiLCJyb2xlcyI6WyJST0xFX1BST0ZFU1NPUiJdLCJpYXQiOjE1OTY3MjMzODUsImV4cCI6MTU5Njc1OTM4NX0.CB2UIP1C-Ru2whr0K0O6f3WaqSeoIqd6rnb94wbsEzw"
    s1_token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzMUBzdHVkZW50aS5wb2xpdG8uaXQiLCJyb2xlcyI6WyJST0xFX1NUVURFTlQiXSwiaWF0IjoxNTk2ODEyMTA3LCJleHAiOjE1OTY4NDgxMDd9.qGJ-ZgxyW9CCHnM1wCuQUVIUCyy-efonWBSDlqu_o7w"
    s2_token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzMkBzdHVkZW50aS5wb2xpdG8uaXQiLCJyb2xlcyI6WyJST0xFX1NUVURFTlQiXSwiaWF0IjoxNTk2ODEyMTQ1LCJleHAiOjE1OTY4NDgxNDV9.ySPsf2GQOlCVpqP1QJyuiPXXDZmzuxV6kQ3fFO37nC4"
    d2_token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkMkBwb2xpdG8uaXQiLCJyb2xlcyI6WyJST0xFX1BST0ZFU1NPUiJdLCJpYXQiOjE1OTY2MzY3MTIsImV4cCI6MTU5NjY3MjcxMn0.adY9W4UVT7p1_BMtGMVIS3yXl8s5rpejham0yUeciJU"

    #res = authenticate(s2_account['username'], s2_account['password'])
    res = deleteVM("AI", "1", 5, "s2", s2_token)
    #res = createVmInstance("ai", 1, "s2", 4, 1024*6, 16, s2_token)

    print(res.status_code)
    print(res.text)
