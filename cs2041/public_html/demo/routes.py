from server import app
from flask import redirect, request, render_template
from models import Event

CONFIG = {
        "private_key" : "sdfjjfdskfhwui21e2i3u84923"
}

@app.route('/', methods=["POST", "GET"])
def index():
    msg = ""
    if request.method == "POST":
        name = request.form['name']
        print(name)
        msg = name + ", thank you for signing up for {event.name} which will be held on {event.date}"
        event = Event(1,2,3, "Cool", "12/05/2018")
        return render_template('index.html', msg=msg.format(event=event))
    return render_template('index.html', msg=msg)
