package com.example.demo.controller;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.BookDAO;
import com.example.demo.vo.Book;
import com.example.demo.vo.NewBook;

@RestController
public class HanbController {

	@Autowired
	private BookDAO dao;
	
	@PostMapping("/search")
    public String search(String keyword) {
        String base = "https://www.hanbit.co.kr";
        String url = "https://www.hanbit.co.kr/store/books/new_book_list.html?keyWord="+keyword+"&searchKey=p_title";
        try {
            Document doc= Jsoup.connect(url).get();
            Elements list =  doc.select(".book_tit");
            for(Element e :list) {
                //book_tit의 첫번째 자식태그a를 찾는다
            	Element a = e.firstElementChild();
            	
                //a태그의 글자와 href를 갖고온다.
                String title = a.text();
                String link = a.attr("href");
                
                //  /store/books/look.php?p_code=B6171497304
                //=뒤에서부터 짤라서 책번호 가져온다.
                String p_code = link.substring(link.indexOf("=")+1);
                //base와 경로에 link를 연결하여 책 상세보기 url를 만들어준다.
                String url2 = base + link;
                //책상세보기 url2에 연결하여 문서를 갖고온다.
                Document doc2= Jsoup.connect(url2).get();
                Elements li =
                doc2.select(".info_list").get(0).getElementsByTag("li");
                String writer = li.get(0).getElementsByTag("span").get(0).text();
                String regdate = li.get(1).getElementsByTag("span").get(0).text();
                int price = Integer.parseInt(doc2.select(".pbr").get(0).text().replace(",", "").replace("원", ""));
                System.out.println("---------------------------");
                System.out.println("도서코드:"+p_code);
                System.out.println("도서명:"+title);
                System.out.println("저자:"+writer);
                System.out.println("출간일:"+regdate);
                System.out.println("가격:"+price);
                System.out.println("---------------------------");
                Book book = new Book();
                book.setP_code(p_code);
                book.setTitle(title);
                book.setRegdate(regdate);
                book.setPrice(price);
                book.setWriter(writer);
                dao.save(book);
            }
        }catch (Exception e) {
            System.out.println("예외발생:"+e.getMessage());
        }
        return "OK";
    }
	
	
	@GetMapping("/downAll")
    public String downAll() {
        try {
            int page = 1;//모든페이지를 다운로드하기 위하여 페이지번호를 이용한다.1부터 시작
            while(true) {//모든페이지를 다운로드하기 반복문을 이용한다.
                String url = "https://www.hanbit.co.kr/store/books/new_book_list.html?page="+page;
                Document doc = Jsoup.connect(url).get();
                Elements elements = doc.select(".view_box");//그 페이지 있는 view_box클래스 노드를 찾는다.
                if(elements.size() == 0) {//그 페이지에 view_box가 없으면 탈출한다.
                    break;
                }
                //한페이지에서 책한권의 정보가 .view_box에 담겨있다.
                for(Element e:elements) {//view_box노드만큼 반복실행
                    Elements img  = e.getElementsByTag("img");
                    //한권의 책 정보가 담겨 있는 view_box에서 img태그를 찾는다.
                    
                    String src = img.get(img.size()-1).attr("src");
                    //img태그에서 src속성을 갖고 온다.
                    
                    String title =
                    e.select(".book_tit").get(0).getElementsByTag("a").get(0).text();
                    //한권의 책 정보가 담겨있는 view_box에서 a를 찾고 text를 갖고온다.
                    //책이름
                    
                    imageDownload("https://www.hanbit.co.kr"+src, title);
                    //imageDownload메소드를 호출할때 이미지경로와 
                }
                System.out.println(page+"페이지를 다운로드하였습니다.");
                System.out.println("-------------------------------");
                page++;
            }
        }catch (Exception e) {
            System.out.println("예외발생:"+e.getMessage());
        }
        return "OK";
    }
	
	//이미지가 있는 url과 저장할 파일명을 매개변수로 전달받아 파일을 다운로드 하는 메소드
	public void imageDownload(String addr, String fname) {
		
		//파일명의 특수문자를 치환
        fname = fname.replace("/", "_");
        fname = fname.replace("?", "_");
        fname = fname.replace("#", "_");
        fname = fname.replace(":", "_");
        try {
        	//이미지를 다운로드하기 위하여 
            URL url = new URL(addr);
            //인터넷사의 문서 url의 내용을 메모리로 읽어들이기 위한 스트림생성
            InputStream is = url.openStream();
            //내컴퓨터를 출력하기 위한 스트림생성
            //이때 파일명을 매개변수로 전달받은 책임으로한다.
            FileOutputStream fos = new FileOutputStream("c:/data/"+fname+".jpg");
            //스프링이 제공하는 FileCopyUtil을 이용하여 파일을 복사한다.
            FileCopyUtils.copy(is.readAllBytes(), fos);
            is.close();
            fos.close();
            System.out.println(fname+"을 저장하였습니다.");
        }catch (Exception e) {
            System.out.println("예외발생:"+e.getMessage());
        }
    }
	
	@GetMapping("/downImg")
	public String downImg() {
		String data = "OK";
		String addr = "https://www.hanbit.co.kr/data/books/B9711360107_m.jpg";
		try {
			String fname="두부의캐릭터드로잉.jpg";
			URL url = new URL(addr);
			InputStream is = url.openStream();
			FileOutputStream fos = new FileOutputStream("c:/data/"+fname);
			FileCopyUtils.copy(is.readAllBytes(), fos);
			is.close();
			fos.close();
			System.out.println("이미지를 다운로드 하였습니다.");
		}catch(Exception e){
			System.out.println("예외발생 : "+e.getMessage());
		}
		return data;
	}
	
	@GetMapping("/seat")
	public String getSeat() {
		String data="";
		String url="http://mpllc-seat.sen.go.kr/seatinfo/Seat_Info/1_count.asp";
		//waiting_f
		try {
			Document doc=Jsoup.connect(url).get();
			data=doc.select(".wating_f").get(0).text();
			System.out.println(data);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return data;
	}
//	@GetMapping("/newBook")
//	public String newBook() {
//		String url = "https://www.hanbit.co.kr/store/books/new_book_list.html";
//		String base = "https://www.hanbit.co.kr/";
//		try {
//			//Jsoup을 이용하여 url(한빛출판사 새책의 url)연결하여 문서를 읽어온다.
//			Document doc = Jsoup.connect(url).get();
//			System.out.println(doc);
//			Elements list = doc.select(".book_tit");
//			for(Element e : list) {
//				Element a = e.firstElementChild();
//				String title = a.text();
//				String link = base + a.attr("href");
//				System.out.println(title);
//				System.out.println(link);
//				System.out.println("---------------------------");
//			}
//			
//		}catch(Exception e) {
//			System.out.println("Contrl newBook 에외"+e.getMessage());
//		}
//		return "OK";
//	}
	
	@GetMapping("/newBook")
	public List<NewBook> newBook() {
		List<NewBook> arr = new ArrayList<NewBook>();
		int i=1;
		while(true) {
			String url = "https://hanbit.co.kr/store/books/new_book_list.html?page="+i;
			String base = "https://www.hanbit.co.kr/";
			try {
				//Jsoup을 이용하여 url(한빛출판사 새책의 url)연결하여 문서를 읽어온다.
				Document doc = Jsoup.connect(url).get();
				Elements list = doc.select(".book_tit");
				//더이상 select되어 가져오는 list가 없으면 while문 탈출한다.!
				if(list.size()==0) {
					break;
				}
				for(Element e : list) {
					Element a = e.firstElementChild();
					String title = a.text();
					String link = base + a.attr("href");
					arr.add(new NewBook(title,link));
				}
				
			}catch(Exception e) {
				System.out.println("Contrl newBook 에외"+e.getMessage());
			}
			System.out.println(i+"페이지를 수집하였습니다.");
			i++;
		}
		return arr;
	}
}
