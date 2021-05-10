import React, {Component} from 'react';
import axios from 'axios';
import {DropzoneArea} from 'material-ui-dropzone';
import './UploadArea.css';

class UploadArea extends Component {
  constructor(props) {
    super(props);

    this.state = {
      files: [],
      transactions: []
    };
  }

  handleChange = (files) => {
    this.setState({
      files: files,
      transactions: this.state.transactions
    });
  };

  onClickHandler = () => {
    this.state.files.map(file => {
      const data = new FormData()
      data.append('file', file)
      axios.post("http://localhost:8080/upload", data, {})
        .then(response => {
          console.log(response.statusText)
        })
    });
  };

  getTransactionsHandler = () => {
      axios.get("http://localhost:8080/transactions", {})
        .then(response => {
          console.log(response)
        })
  };


  render() {
    const { isLoading, error } = this.state;

    if (isLoading) {
      return <p>Loading ...</p>;
    }

    if (error) {
      return <p>Error connecting to server</p>;
    }

    const transactions = this.state.transactions.map( transaction =>
      <li key={transaction.id}>{transaction.transactionDate} - {transaction.description} - {transaction.netAmount} - {transaction.currency}</li>
    );

    return (
      <div>
        <DropzoneArea
          acceptedFiles={['application/pdf', 'application/x-pdf', 'text/csv', 'application/vnd.ms-excel', 'application/vnd.msexcel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet']}
          filesLimit={100000}
          maxFileSize={25000000}
          dropzoneText={'.csv, .pdf, .xlsx...'}
          showFileNames={true}
          onChange={this.handleChange.bind(this)}
        />
        <div className={"btn-div"}>
          <button type="button" className="btn btn-success btn-block btn-gap" onClick={this.onClickHandler}>Upload</button>
        </div>
        <div className={"btn-div"}>
          <button type="button" className="btn btn-success btn-block btn-gap" onClick={this.getTransactionsHandler}>Get Transactions</button>
        </div>
        <div>
          <ul>
            {transactions}
          </ul>
        </div>

      </div>
    );
  }
}

export default UploadArea;
