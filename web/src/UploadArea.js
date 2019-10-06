import React, {Component} from 'react';
import {DropzoneArea} from 'material-ui-dropzone'

const API_URL = 'http://localhost:8080/upload';

class UploadArea extends Component {
  constructor(props) {
    super(props);

    this.state = {
      files: []
    };
  }

  handleChange(files){
    this.setState({
      files: files
    });
  }

  render() {
    const { message, isLoading, error } = this.state;

    if (isLoading) {
      return <p>Loading ...</p>;
    }

    if (error) {
      return <p>Error uploading file</p>;
    }

    return (
      <DropzoneArea
        onChange={this.handleChange.bind(this)}
      />
    );
  }

}

export default UploadArea;
