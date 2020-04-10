import React, {Component} from 'react';
import axios from 'axios';
import {DropzoneArea} from 'material-ui-dropzone';
import './UploadArea.css';

class UploadArea extends Component {
  constructor(props) {
    super(props);

    this.state = {
      files: []
    };
  }

  handleChange = (files) => {
    this.setState({
      files: files
    });
  };

  onClickHandler = () => {
    const data = new FormData()
    data.append('file', this.state.files[0])
    axios.post("http://localhost:8080/upload", data, {})
      .then(response => {
        console.log(response.statusText)
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

    return (
      <div>
        <DropzoneArea
          acceptedFiles={['application/pdf', 'application/x-pdf', 'text/csv']}
          filesLimit={100000}
          maxFileSize={25000000}
          dropzoneText={'PDF or CSV'}
          showFileNames={true}
          onChange={this.handleChange.bind(this)}
        />
        <div>
          <button type="button" className="btn btn-success btn-block btn-gap" onClick={this.onClickHandler}>Upload</button>
        </div>


      </div>
    );
  }
}

export default UploadArea;
