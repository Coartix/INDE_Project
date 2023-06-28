from flask import Flask, request, jsonify
from flask_cors import CORS  # Import the CORS module

app = Flask(__name__)
CORS(app)

reports = []


@app.route('/receive-report', methods=['POST'])
def receive_report():
    report = request.get_json()
    reports.append(report)
    return jsonify({'message': 'Report received successfully.'}), 200


@app.route('/reports', methods=['GET'])
def get_reports():
    return jsonify(reports), 200


if __name__ == '__main__':
    app.run(debug=True)
