import React, {Component} from 'react';
import {DropzoneArea} from 'material-ui-dropzone'

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
    const { isLoading, error } = this.state;

    if (isLoading) {
      return <p>Loading ...</p>;
    }

    if (error) {
      return <p>Error connecting to server</p>;
    }

    return (
      <DropzoneArea
        acceptedFiles={['application/pdf', 'application/x-pdf', 'text/csv']}
        filesLimit={100000}
        maxFileSize={25000000}
        dropzoneText={'PDF or CSV'}
        showFileNames={true}
        onChange={this.handleChange.bind(this)}
      />
    );
  }
}

export default UploadArea;
