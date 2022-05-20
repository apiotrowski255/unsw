from flask import Flask

app = Flask(__name__)
app.config['SECRET_KEY'] = "9847r9824u"


CONFIG = {
        "private_key" : "sdfjjfdskfhwui21e2i3u84923"
}

class Event:
    def __init__(self, a, b, c, name, date):
        self.a = a
        self.b = b
        self.c = c
        self.name = name
        self.date = date


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
from routes import app

if __name__=="__main__":
    app.run(debug=True)
