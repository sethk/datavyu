package org.datavyu.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.avutil;

import java.io.File;

import static org.bytedeco.javacpp.avcodec.avcodec_find_decoder;
import static org.bytedeco.javacpp.avcodec.avcodec_open2;
import static org.bytedeco.javacpp.avformat.*;

public class FrameRate {


  private avformat.AVFormatContext pFormatCtx;
  private File file;
  private avcodec.AVCodecContext pCodecCtxOrig = null;
  private avcodec.AVCodecContext pCodecCtx = null;
  private avcodec.AVCodec pCodec = null;
  private avutil.AVDictionary optionsDict = null;
  private avutil.AVFrame pFrame;
  private AVStream videoAVStream;
  private static Logger logger = LogManager.getFormatterLogger(FrameRate.class);

  private String testFile = "DatavyuSampleVideo.mp4";

  public static FrameRate createDefaultFrameRate(File file){ return new FrameRate(file); }

  private FrameRate(File file){ 
//    this.file = file;
    init();
  }

  private void init() {
    pFormatCtx = null; //stores information about the file format in the AVFormatContext structure

    pCodecCtxOrig = null;
    pCodecCtx = null;
    pCodec = null;
    optionsDict = null;

    int videoStream = -1;

    pFrame = null;



    /**
     * This registers all available file formats
     * and codecs with the library so they will be
     * used automatically when a file with the corresponding
     * format/codec is opened.
     */
    av_register_all();

    /**
     * This function reads the file header and stores information
     * about the file format in the AVFormatContext structure we have
     * given it. The last three arguments are used to specify the file
     * format, buffer size, and format options, but by setting this
     * to NULL or 0, libavformat will auto-detect these.
     * This function only looks at the header, so next we need to check
     * out the stream information in the file
     */
    if(avformat_open_input(pFormatCtx, testFile, null, null)!=0){
      logger.error("Can't read the file header");
      throw new IllegalStateException("Can't read the file: "+ file.toString());
    }

    /**
     * Retrieve Stream information, this function populates
     * pFormatCtx->streams with the proper information.
     */
    if(avformat_find_stream_info(pFormatCtx, (PointerPointer) null) < 0){
      logger.error("Can't retrieve stream information");
      throw new IllegalStateException("Can't retrieve stream information "+ file.toString());
    }

    /**
     * Dump information about file onto standard error,
     * Now pFormatCtx->streams is just an array of
     * pointers, of size pFormatCtx->nb_streams,
     */
    av_dump_format(pFormatCtx, 0, testFile, 0);

    // Find the first video stream

    for (int i = 0; i < pFormatCtx.nb_streams(); i++) {
      videoAVStream = pFormatCtx.streams(i);
      if (videoAVStream.codecpar().codec_type() == avutil.AVMEDIA_TYPE_VIDEO) {
        videoStream = i;
        break;
      }
    }

    if (videoStream == -1) {
      logger.error("Didn't find a video stream");
      throw new IllegalStateException("Didn't find a video stream in "+ file.toString());
    }


    /**
     * The stream's information about the codec is in what we
     * call the "codec context." This contains all the information
     * about the codec that the stream is using, and now we have
     * a pointer to it. But we still have to find the
     * actual codec and open it
     */
    // Get a pointer to the codec context for the video stream
    pCodecCtx = pFormatCtx.streams(videoStream).codec();


    // Find the decoder for the video stream
    pCodec = avcodec_find_decoder(pCodecCtx.codec_id());

    if (pCodec == null) {
      logger.error("Unsupported codec!");
      throw new IllegalStateException("Unsupported codec!"  );
    }

    // Open codec
    if (avcodec_open2(pCodecCtx, pCodec, optionsDict) < 0) {
      logger.error("Could not open video codec");
      throw new IllegalStateException("Could not open video codec");
    }

  }

  public double getFPS() {
    if(videoAVStream == null) {
      return 30;
    } else {
      avutil.AVRational rational = videoAVStream.avg_frame_rate();
      if(rational.num() == 0 && rational.den() == 0) {
        rational = videoAVStream.r_frame_rate();
      }
      logger.info("FPS: " +(double) rational.num() / rational.den());
      return (double) rational.num() / rational.den();
    }
  }
}
