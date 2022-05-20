from flask import Flask, render_template
from data import data

app = Flask(__name__)

data = data()


@app.route('/helloWorld')
def index():
   return render_template('helloWorld.html')


if __name__ == '__main__':
   app.run(debug=True)
