import requests

students = "http://localhost:8080/API/students/"
courses = "http://localhost:8080/API/courses/"


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


if __name__ == '__main__':
    s1_account = {"username": "s1@studenti.polito.it", "password": "oj3SOdn$)2"}
    d1_account = {"username": "d1@polito.it", "password": "x,5DW)0hP3"}
    d2_account = {"username": "d2@polito.it", "password": "QJ*2-W4ezo"}
    admin_token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIlJPTEVfQURNSU4iXSwiaWF0IjoxNTg5OTkxODg1LCJleHAiOjE1ODk5OTU0ODV9.kxE8T6b_Vzh9KJfh-S-LPYSEqNIw1s7yNmlEb4xzbVM"
    d1_token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkMUBwb2xpdG8uaXQiLCJyb2xlcyI6WyJST0xFX1BST0ZFU1NPUiJdLCJpYXQiOjE1ODk5OTI5MjksImV4cCI6MTU4OTk5NjUyOX0.GHgxRIDPqNVZuZCNTj4mDTJKTD4X_3Uog67enUUYIrc"
    s1_token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzMUBzdHVkZW50aS5wb2xpdG8uaXQiLCJyb2xlcyI6WyJST0xFX1NUVURFTlQiXSwiaWF0IjoxNTg5OTkwMjk1LCJleHAiOjE1ODk5OTM4OTV9.Xhe6OQK5gIH70r8SEtIu67DwLLNkKhUvtlchXbUL4O4"
    d2_token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkMkBwb2xpdG8uaXQiLCJyb2xlcyI6WyJST0xFX1BST0ZFU1NPUiJdLCJpYXQiOjE1ODk5OTI4MjIsImV4cCI6MTU4OTk5NjQyMn0.s2H_qtYAJUWZ9MgPcVbO7jFVGmb1tudHhX46DKZblNc"
    s3_token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzM0BzdHVkZW50aS5wb2xpdG8uaXQiLCJyb2xlcyI6WyJST0xFX1NUVURFTlQiXSwiaWF0IjoxNTg5NjQ1ODI2LCJleHAiOjE1ODk2NDk0MjZ9.aQeVEUD_lB3sPAKVM_2MI_zuxGHoVR4O1x-b8po3z1w"

    #res=authenticate(d1_account['username'], d1_account['password'])
    #res = authenticate("admin", "admin")
    #res = addProfessor("d3", "Gianpiero", "Cabodi", admin_token)
    res = enableDisableCourse("applicazioni Internet", True, d1_token)
    print(res.status_code)
    print(res.text)
